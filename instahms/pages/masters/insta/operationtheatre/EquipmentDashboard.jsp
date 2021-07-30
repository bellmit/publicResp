<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/addEquipmet.js" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />

<c:set value="${pageContext.request.contextPath}" var="cpath" />
<style type="text/css">
	.status_I{background-color: #E4C89C}
</style>

</head>
<body onload="populteEquipmentNames();selectBedTypes();"
	class="yui-skin-sam">
<html:form action="/pages/masters/insta/operationtheatre/Equipment1.do"
	onsubmit="return searchValidate()" method="GET">
	<html:hidden property="pageNum" />
	<input type="hidden" name="method" value="getEquipment" />
	<c:url var="EC" value="Equipment1.do">
		<c:param name="method" value="geteditChargeScreen" />
	</c:url>

	<table width="100%" class="formtable">

		<tr>
			<th class="pageheader" align="center">Equipment Master</th>
		</tr>

		<tr><td colspan="2" align="center"><insta:feedback-panel/></td></tr>

		<tr>
			<th>&nbsp;</th>
		</tr>

		<tr>
			<td>
			<table class="formtable" align="center">
				<tr>
					<th>Rate Sheet</th>
					<td>
						<insta:selectdb name="orgId" value="${equipmentform.orgId}"
							table="organization_details" valuecol="org_id" orderby="org_name"
							displaycol="org_name" onchange="changeRate();"
							filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
					</td>
					<th>View Charges For</th>
					<td><select name="chargeType">
						<c:if test="${param.chargeType ne null && not empty param.chargeType}">
						 	<c:choose>
						 	 	<c:when test="${param.chargeType eq 'DC'}">
									<option value="DC" onclick="changeRate();">Daily Charge </option>
							  	</c:when>
							  	<c:when test="${param.chargeType eq 'HC'}">
									<option value="HC" onclick="changeRate();">(Hourly)Min Charge</option>
							 	 </c:when>
							  	<c:when test="${param.chargeType eq 'IC'}">
							  		<option value="IC" onclick="changeRate();">(Hourly)Incr Charge</option>
							  	</c:when>
					 		</c:choose>
							</c:if>
							<c:if test="${param.chargeType ne 'DC'}">
								<option value="DC" onclick="changeRate();">Daily Charge </option>
							</c:if>
							<c:if test="${param.chargeType ne 'HC'}">
								<option value="HC" onclick="changeRate();">(Hourly)Min Charge</option>
							</c:if>
							<c:if test="${param.chargeType ne 'IC'}">
								<option value="IC" onclick="changeRate();">(Hourly)Incr Charge </option>
							</c:if>
					</select></td>
				</tr>
			</table>
			</td>
		</tr>

		<tr>
			<td>
			<table align="left" width="100%">
				<tr>
					<td>
						<div class="stwMain">
						<div class="stwHeader ${filterClosed ? 'stwClosed' : ''}" id="filter"
							onclick="stwToggle(this);"><label>Filter</label></div>
						<div id="filter_content"
							class="stwContent ${filterClosed ? 'stwHidden' : ''}">
						<table  class="search"
							title="Filter Equipments">
							<tr>
								<th>Department</th>
								<th>Status</th>
								<th>Equipment Name</th>
							</tr>
							<tr>
								<td><html:select property="deptFilter" multiple="true"
									size="4">
									<c:forEach var="item" items="${requestScope.departments}">
										<html:option value="${item.DEPT_ID}">${item.DEPT_NAME} </html:option>
									</c:forEach>
								</html:select></td>
								<td><html:checkbox property="allEquipements" value="ALL">All Equipments</html:checkbox><br />
								<html:checkbox property="activeEquipments" value="A">Active Equipments</html:checkbox><br />
								<html:checkbox property="inactiveEquipments" value="I">Inactive Equipments</html:checkbox><br />
								</td>
								<td>
								<div id="myAutoComplete"><html:text
									property="equipmentFilter" styleId="equipmentFilter"></html:text>
								<div id="equipmentContainer"></div>
								</div>
								</td>

							</tr>

							<tr>
								<td colspan="6" align="right"><input type="submit"
									name="search" value="Search" /></td>
							</tr>

						</table>
						</div>
						</div>
					</td>
				</tr>
			</table>
			</td>
		</tr>
		<c:set var="equipmentList" value="${pagedList.dtoList}" />
		<tr>
			<td>
			<table class="dashboard" align="center" width="100%">
				<c:forEach var="map" items="${equipmentList}">
					<c:set var="rowCount" value="1" />
					<%--daily charge --%>
					<c:forEach var="chargeList" items="${map.dailyCharge}">
						<c:choose>
							<c:when test="${rowCount eq 1}">
								<tr>
									<c:forEach var="bed" items="${chargeList}">
										<th>${bed}</th>
									</c:forEach>
								</tr>
								<c:set var="rowCount" value="2" />
								<input type="hidden" name="groupUpdatComponent" value="DC" />
							</c:when>
							<c:otherwise>
								<tr class="status_${chargeList[0]}">
									<c:set var="colCount" value="0" />
									<c:forEach var="charge" items="${chargeList}">
										<c:choose>
										   <c:when test="${colCount eq 0}">
												<c:set var="colCount" value="1" />
										   </c:when>
											<c:when test="${colCount eq 1}">
												<td><input type="checkbox" name="groupEquipments"
													value="${charge}"></td>
												<c:set var="colCount" value="2" />
												<c:set var="equipmentId" value="${charge}" />
											</c:when>
											<c:when test="${colCount eq 2}">
												<td><a title="Edit Equipment Details"
													href='<c:out value="${ifn:cleanURL(EC)}&equipmentId=${ifn:cleanURL(equipmentId)}&orgId=${equipmentform.orgId}
													&orgName=${equipmentform.orgName}&chargeType=${equipmentform.chargeType}
													&pageNum=${equipmentform.pageNum}"/>'>${charge}</td>
												<c:set var="colCount" value="3" />
											</c:when>
											<c:otherwise>
												<td class="number">${ifn:afmt(charge)}</td>
											</c:otherwise>
										</c:choose>
									</c:forEach>
								</tr>
							</c:otherwise>
						</c:choose>
					</c:forEach>

					<%--hourly charge --%>
					<c:forEach var="chargeList" items="${map.hourlyCharge}">
						<c:choose>
							<c:when test="${rowCount eq 1}">
								<tr>
									<c:forEach var="bed" items="${chargeList}">
										<th>${bed}</th>
									</c:forEach>
								</tr>
								<c:set var="rowCount" value="2" />
								<input type="hidden" name="groupUpdatComponent" value="HC" />
							</c:when>
							<c:otherwise>
								<tr class="status_${chargeList[0]}">
									<c:set var="colCount" value="0" />
									<c:forEach var="charge" items="${chargeList}">
										<c:choose>
											<c:when test="${colCount eq 0}">
												<c:set var="colCount" value="1" />
											</c:when>
											<c:when test="${colCount eq 1}">
												<td><input type="checkbox" name="groupEquipments"
													value="${charge}"></td>
												<c:set var="colCount" value="2" />
												<c:set var="equipmentId" value="${charge}" />
											</c:when>
											<c:when test="${colCount eq 2}">
												<td><a title="Edit Equipment Details"
													href='<c:out value="${ifn:cleanURL(EC)}&equipmentId=${ifn:cleanURL(equipmentId)}&orgId=${equipmentform.orgId}
													&orgName=${equipmentform.orgName}&chargeType=${equipmentform.chargeType}
													&pageNum=${equipmentform.pageNum}"/>'>${charge}</td>
												<c:set var="colCount" value="3" />
											</c:when>
											<c:otherwise>
												<td class="number">${ifn:afmt(charge)}</td>
											</c:otherwise>
										</c:choose>
									</c:forEach>
								</tr>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					<%--Incremental  charge --%>

					<c:forEach var="chargeList" items="${map.incrCharge}">
						<c:choose>
							<c:when test="${rowCount eq 1}">
								<tr>
									<c:forEach var="bed" items="${chargeList}">
										<th>${bed}</th>
									</c:forEach>
								</tr>
								<c:set var="rowCount" value="2" />
								<input type="hidden" name="groupUpdatComponent" value="IC" />
							</c:when>
							<c:otherwise>
								<tr class="status_${chargeList[0]}">
									<c:set var="colCount" value="0" />
									<c:forEach var="charge" items="${chargeList}">
										<c:choose>
											<c:when test="${colCount eq 0}">
												<c:set var="colCount" value="1" />
											</c:when>
											<c:when test="${colCount eq 1}">
												<td><input type="checkbox" name="groupEquipments"
													value="${charge}"></td>
												<c:set var="colCount" value="2" />
												<c:set var="equipmentId" value="${charge}" />
											</c:when>
											<c:when test="${colCount eq 2}">
												<td><a title="Edit Equipment Details"
													href='<c:out value="${ifn:cleanURL(EC)}&equipmentId=${ifn:cleanURL(equipmentId)}&orgId=${equipmentform.orgId}
													&orgName=${equipmentform.orgName}&chargeType=${equipmentform.chargeType}
													&pageNum=${equipmentform.pageNum}"/>'>${charge}</td>
												<c:set var="colCount" value="3" />
											</c:when>
											<c:otherwise>
												<td class="number">${ifn:afmt(charge)}</td>
											</c:otherwise>
										</c:choose>
									</c:forEach>
								</tr>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</c:forEach>
			</table>
			</td>
		</tr>
		<tr>
			<td align="center"><c:if test="${pagedList.numPages gt 1}">
				<c:forEach var="page" begin="1" end="${pagedList.numPages}">
					<c:choose>
						<c:when test="${pagedList.pageNumber == page}">
							<b>${page}</b>
						</c:when>
						<c:otherwise>
							<b><a href="#" onclick="return checkPageNum('${page}');">${page}</a></b>
						</c:otherwise>
					</c:choose>
				</c:forEach>
			</c:if></td>
		</tr>

		<tr>
			<td>
			<table title="GROUPE UPDATE" class="dashboard" border="2">
				<tr>
					<th colspan="2">GROUP UPDATE/ Select All Equipments:<input
						type="checkbox" name="All" onchange="selectAll()"></th>
				</tr>
				<tr>
					<td>Bed Type  &nbsp;&nbsp;
					<input type=checkbox name="allBedTypes" title="To select all bed types." checked="false" onclick="selectBedTypes();"/> All</td>
					<td>Rate Variance</td>
				</tr>
				<tr>
					<td><select multiple="true" size="5" name="groupBeds">
						<c:forEach items="${bedTypes}" var="bed">
							<option value="${bed.BED_TYPE}" onclick="deselectbedtypes();">${bed.BED_TYPE}</option>
						</c:forEach>
					</select></td>
					<td><html:select property="varianceType" style="width:9em">
						<html:option value="Incr">Increase By</html:option>
						<html:option value="Decr">Decrease By</html:option>
					</html:select>&nbsp;<input type="text" class="number" name="varianceBy">%
					or Rs <input type="text" class="number" name="varianceValue"></td>
				</tr>
			</table>
			</td>
		</tr>
		<tr>
			<td><input type="button" name="save" value="Update Charges"
				onclick="ValidateGropUpdate()"></td>
		</tr>
		<tr>
			<td align="left">
			 Legend:
				<table class="legend">
					<tr>
						<td class="status_I">
								Inactive Equipments
						</td>
					</tr>
				</table>
			</td>
		</tr>

		<tr>
			<td align="center"><b> <a
				href="${cpath}/pages/masters/insta/operationtheatre/Equipment1.do?method=getNewEquipmentScreen">Add
			New Equipment</a></b></td>
		</tr>
	</table>
	<script>
		var equipmentNames = ${requestScope.equipmentNames};
	</script>

</html:form>
</body>
</html>
