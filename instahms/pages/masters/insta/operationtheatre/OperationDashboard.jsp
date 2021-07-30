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
<insta:link type="js" file="masters/addOperation.js" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />

<c:set value="${pageContext.request.contextPath}" var="cpath" />
<c:set var="chtt" value="${param.chargeType}"/>
<c:set var="sc" value="SC"/>
<c:set var="ac" value="AC"/>
<c:set var="sac" value="SAC"/>
<style type="text/css">
	.status_N {background-color: #E4C89C}
	.status_I { background-color: #C5D9A3;}
</style>


</head>
<body onload="populteOperationNames();"
	class="yui-skin-sam">
<html:form action="/pages/masters/insta/operationtheatre/opmast.do"
	onsubmit="return searchValidate()" method="GET">
	<html:hidden property="pageNum" />
	<html:hidden property="startPage"/>
	<html:hidden property="endPage"/>
		<input type="hidden" name="method" value="getDeptInfo" />
	<c:url var="EC" value="opmast.do">
		<c:param name="method" value="geteditChargeScreen" />
	</c:url>
	<table class="formtable" align="center" width="100%">
		<tr>
			<th class="pageheader" width="100%" align="center">Operation
			Master</th>
		</tr>

		<tr><td><insta:feedback-panel/></td></tr>

		<tr>
			<td>
			<table class="formtable" align="center">
				<tr>
					<th>Rate Sheet</th>
					<td>
						<insta:selectdb name="orgId" value="${OPerMasterForm.orgId}"
							table="organization_details" valuecol="org_id" orderby="org_name"
							displaycol="org_name" onchange="changeRate();"
							filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
					</td>

						<th>View Charges For</th>
						<td>
							<select name="chargeType">
							<c:if test="${param.chargeType ne null && not empty param.chargeType}">
						 	<c:choose>
						 	 	<c:when test="${param.chargeType eq 'SC'}">
									<option value="SC" onclick="changeRate();" >Surgeon Charge </option>
							  	</c:when>
							  	<c:when test="${param.chargeType eq 'AC'}">
									<option value="AC" 	onclick="changeRate();">Anesthetist Charge</option>
							 	 </c:when>
							  	<c:when test="${param.chargeType eq 'SAC'}">
							  		<option value="SAC"	onclick="changeRate();" >Surgical Assistance Charge</option>
							  	</c:when>
					 		</c:choose>
							</c:if>
							<c:if test="${param.chargeType ne 'SC'}">
								<option value="SC" onclick="changeRate();" >Surgeon Charge </option>
							</c:if>
							<c:if test="${param.chargeType ne 'AC'}">
								<option value="AC" 	onclick="changeRate();">Anesthetist Charge</option>
							</c:if>
							<c:if test="${param.chargeType ne 'SAC'}">
								<option value="SAC"	onclick="changeRate();" >Surgical Assistance Charge</option>
							</c:if>
					   		</select>
						</td>
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
							title="Filter Operations" width="60%">
							<tr>
								<th>Department</th>
								<th>Status</th>
								<th>Applicable</th>
								<th>Operation Name</th>
							</tr>
							<tr>
								<td><html:select property="deptFilter" multiple="true"
									size="4">
									<c:forEach var="item" items="${requestScope.departments}">
										<html:option value="${item.DEPT_ID}">${item.DEPT_NAME} </html:option>
									</c:forEach>
								</html:select></td>
								<td><html:checkbox property="allOperations" value="ALL">All Operations</html:checkbox><br />
								<html:checkbox property="activeOperations" value="A">Active Operations</html:checkbox><br />
								<html:checkbox property="inactiveOperations" value="I">Inactive Operations</html:checkbox><br />
								</td>

								<td><html:checkbox property="allAppServices" value="ALL">All</html:checkbox><br />
									<html:checkbox property="appServices" value="Y">Applicable For Org</html:checkbox><br />
									<html:checkbox property="inappServices" value="N">Not Applicable For Org</html:checkbox><br />
								</td>

								<td>
								<div id="myAutoComplete"><html:text
									property="operationFilter" styleId="operationFilter"></html:text>
								<div id="operationContainer"></div>
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

		<c:set var="operationList" value="${pagedList.dtoList}" />
		<%-- ${operationList} --%>
		<tr>
			<td>
			<table class="dashboard" align="center" width="100%">
				<c:forEach var="map" items="${operationList}">
					<c:set var="rowCount" value="1" />
					<%--surgeon charges --%>
					<c:forEach var="chargeList" items="${map.surgeon_charge}">
						<c:choose>
							<c:when test="${rowCount eq 1}">
								<tr>
									<c:forEach var="bed" items="${chargeList}">
										<th><c:out value="${bed}" escapeXml="true"/></th>
									</c:forEach>
								</tr>
								<c:set var="rowCount" value="2" />
								<input type="hidden" name="groupUpdatComponent" value="SC" />
							</c:when>
							<c:otherwise>
								<tr class="status_${chargeList[4]} status_${chargeList[0]}">
									<c:set var="colCount" value="0" />
									<c:forEach var="charge" items="${chargeList}">
										<c:choose>
										   <c:when test="${colCount eq 0}">
												<c:set var="colCount" value="1" />
										   </c:when>
											<c:when test="${colCount eq 1}">
												<td><input type="checkbox" name="groupOperations"
													value="${charge}"></td>
												<c:set var="colCount" value="2" />
												<c:set var="operationId" value="${charge}" />
											</c:when>
											<c:when test="${colCount eq 2}">
												<td><a title="Edit Operation Details"
													href="${ifn:cleanURL(EC)}&OperationId=${operationId}&orgId=${OPerMasterForm.orgId}
													&orgName=${OPerMasterForm.orgName}&chargeType=${OPerMasterForm.chargeType}
													&pageNum=${OPerMasterForm.pageNum}"><c:out value="${charge}" escapeXml="true"/></a></td>
												<c:set var="colCount" value="3" />
											</c:when>

											<c:when test="${colCount eq 3}">
												<td><c:out value="${charge}" escapeXml="true"/></td>
												<c:set var="colCount" value="4" />
											</c:when>

											<c:when test="${colCount eq 4}">
												<c:set var="colCount" value="5" />
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

					<%--anesthetist_charge--%>
					<c:forEach var="chargeList" items="${map.anesthetist_charge}">
						<c:choose>
							<c:when test="${rowCount eq 1}">
								<tr>
									<c:forEach var="bed" items="${chargeList}">
										<th><c:out value="${bed}" escapeXml="true"/></th>
									</c:forEach>
								</tr>
								<c:set var="rowCount" value="2" />
								<input type="hidden" name="groupUpdatComponent" value="AC" />
							</c:when>
							<c:otherwise>
								<tr class="status_${chargeList[4]} status_${chargeList[0]}">
									<c:set var="colCount" value="0" />
									<c:forEach var="charge" items="${chargeList}">
										<c:choose>
   										   <c:when test="${colCount eq 0}">
												<c:set var="colCount" value="1" />
										   </c:when>

											<c:when test="${colCount eq 1}">
												<td><input type="checkbox" value="${charge}"
													name="groupOperations"></td>
												<c:set var="colCount" value="2" />
												<c:set var="operationId" value="${charge}" />
											</c:when>
											<c:when test="${colCount eq 2}">
												<td><a title="Edit Operation Details"
													href="${ifn:cleanURL(EC)}&OperationId=${operationId}&orgId=${OPerMasterForm.orgId}
													&orgName=${OPerMasterForm.orgName}&chargeType=${OPerMasterForm.chargeType}
													&pageNum=${OPerMasterForm.pageNum}"><c:out value="${charge}" escapeXml="true"/></a></td>
												<c:set var="colCount" value="3" />
											</c:when>

											<c:when test="${colCount eq 3}">
												<td>${charge}</td>
												<c:set var="colCount" value="4" />
											</c:when>

											<c:when test="${colCount eq 4}">
												<c:set var="colCount" value="5" />
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
					<%--surg_asstance_charge--%>
					<c:forEach var="chargeList" items="${map.surg_asstance_charge}">
						<c:choose>
							<c:when test="${rowCount eq 1}">
								<tr>
									<c:forEach var="bed" items="${chargeList}">
										<th><c:out value="${bed}" escapeXml="true"/></th>
									</c:forEach>
								</tr>
								<c:set var="rowCount" value="2" />
								<input type="hidden" name="groupUpdatComponent" value="SAC" />
							</c:when>
							<c:otherwise>
								<tr class="status_${chargeList[4]} status_${chargeList[0]}">
									<c:set var="colCount" value="0" />
									<c:forEach var="charge" items="${chargeList}">
										<c:choose>
   										   <c:when test="${colCount eq 0}">
												<c:set var="colCount" value="1" />
										   </c:when>

											<c:when test="${colCount eq 1}">
												<td><input type="checkbox" value="${charge}"
													name="groupOperations"></td>
												<c:set var="colCount" value="2" />
												<c:set var="operationId" value="${charge}" />
											</c:when>
											<c:when test="${colCount eq 2}">
												<td><a title="Edit Operation Details"
													href="${ifn:cleanURL(EC)}&OperationId=${operationId}&orgId=${OPerMasterForm.orgId}
													&orgName=${OPerMasterForm.orgName}&chargeType=${OPerMasterForm.chargeType}
													&pageNum=${OPerMasterForm.pageNum}"><c:out value="${charge}" escapeXml="true"/></a></td>
												<c:set var="colCount" value="3" />
											</c:when>

											<c:when test="${colCount eq 3}">
												<td><c:out value="${charge}" escapeXml="true"/></td>
												<c:set var="colCount" value="4" />
											</c:when>
											<c:when test="${colCount eq 4}">
												<c:set var="colCount" value="5" />
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
			<td>
				<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
			</td>
		</tr>

		<tr>
			<td>
			<table title="GROUPE UPDATE" class="dashboard" border="2">
				<tr>
					<th colspan="2">GROUP UPDATE/ Select All Operations:
						<input type="checkbox" name="All" onchange="selectAll()">
							&nbsp; &nbsp;
						 Select All ${allOperationsCount} Operations:
					    <input type="checkbox" name="updateallOperations" onchange="selectAllOperations()"
						    value="true"/>
					</th>
				</tr>
				<tr>
					<td>Bed Type &nbsp;
					<input type=checkbox name="allBedTypes" title="To select all bed types." checked="false" onclick="selectBedTypes();"/> All</td>
					<td>Rate Variance</td>
				</tr>
				<tr>
					<td><select multiple="true" size="5" name="groupBeds" title="Ctrl+click to select multiple">
						<c:forEach items="${bedTypes}" var="bed">
							<option value="${bed.BED_TYPE}" onclick="deselectAllBedTypes();">${bed.BED_TYPE}</option>
						</c:forEach>
						</select></td>
					<td><select name="varianceType" style="width:9em">
						<option value="Incr" onclick="disableVarianceBy();">Increase By</option>
						<option value="Decr" onclick="disableVarianceBy();">Decrease By</option>
						</select>&nbsp;<input type="text" name="varianceBy" class="number"/>%
					or Rs <input type="text" name="varianceValue" class="number"/></td>
				</tr>
			</table>
			</td>
		</tr>

		<tr>
			<td>
				<input type="button" name="save" value="Update Charges"
					onclick="ValidateGropUpdate()">
			</td>
		</tr>

		<tr>
			<td align="left">
			 Legend:
				<table class="legend">
					<tr>
						<td class="status_I">
								Inactive Operations
						</td>
					</tr>

					<tr>
						<td class="status_N">
								Not Applicable For Rate Sheet
						</td>
					</tr>
				</table>
			</td>
		</tr>


		<tr>
			<td align="center" ><b><a
				href="${cpath}/pages/masters/insta/operationtheatre/opmast.do?
				method=getNewOperationScreen&applicable=true">Add
			New Operation</a></b></td>
		</tr>

	</table>
	<script>
		var operationNames = ${requestScope.operationNames};
	</script>

</html:form>
</body>
</html>
