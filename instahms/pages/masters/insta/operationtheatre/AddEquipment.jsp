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
<insta:link type="js" file="ajax.js" />

<c:set value="${pageContext.request.contextPath}" var="cpath" />
</head>
<body onload="keepbackupVariable()">
<html:form action="/pages/masters/insta/operationtheatre/Equipment1.do">
	<html:hidden property="pageNum" />
	<html:hidden property="chargeType" />
	<html:hidden property="orgId" />
	<input type="hidden" name="method" value="${requestScope.method}" />
	<html:hidden property="equipmentId" />
	<input type="hidden" name="contextPath"
		value="${pageContext.request.contextPath}" />
	<input type="hidden" name="equipID" value="${ifn:cleanHtmlAttribute(param.equipmentId)}"/>
	<input type="hidden" name="chargeTyp" value="${ifn:cleanHtmlAttribute(param.chargeType)}" />
	<input type="hidden" name="pageNo" value="${ifn:cleanHtmlAttribute(param.pageNum)}" />
	<table class="formtable" align="center">
		<tr>
			<th align="center" colspan="4">Equipment Details Screen</th>
		</tr>

		<tr>
			<th>&nbsp;</th>
		</tr>

		<tr>
			<td>Equipment Name</td>
			<td><html:text property="equipmentName" styleId="equipmentName"
				onblur="checkDuplicate();capWords(equipmentName);" maxlength="150"></html:text></td>
			<td>Department</td>
			<td><html:select property="deptid">
				<html:option value="">--Department--</html:option>
				<c:forEach var="item" items="${requestScope.departments}">
					<html:option value="${item.DEPT_ID}">${item.DEPT_NAME} </html:option>
				</c:forEach>
			</html:select></td>

		</tr>

		<tr>
			<td>Rates For organization:</td>
			<td>
			<c:choose>
			<c:when test="${param.method ne 'getNewEquipmentScreen'}">
			<insta:selectdb name="ratePlan" value="${equipmentform.orgId}"
							table="organization_details" valuecol="org_id" orderby="org_name"
							displaycol="org_name" onchange="changeRatePlan();"/>
			</c:when>
			<c:otherwise>
			<html:text property="orgName" readonly="true" /></td>
			</c:otherwise>
			</c:choose>
			<td>Status</td>
			<td><html:select property="status" style="width:9em;">
				<html:option value="A">Active</html:option>
				<html:option value="I">Inactive</html:option>
			</html:select></td>
		</tr>
		<tr>
			<td>Min Duration:</td>
			<td><html:text property="minDuration" styleClass="number"
				maxlength="3" onkeypress="return enterNumOnlyzeroToNine(event)" />:Hr</td>
			<td>Incr Duration</td>
			<td><html:text property="incrDuration" styleClass="number"
				onkeypress="return enterNumOnlyzeroToNine(event)" maxlength="3" />:Hr</td>
		</tr>

		<tr>
			<td>Description(Code#)</td>
			<td><html:text property="equipmentCode" styleId="equipmentCode"
				size="8" maxlength="50" onblur="upperCase(equipmentCode)" /></td>
		</tr>


		<tr>
			<td colspan="4">
			<table class="dashboard" align="center">
				<c:forEach var="entry" items="${requestScope.map}">
					<c:choose>
						<c:when test="${entry.key eq 'CHARGES'}">
							<tr>
								<th>CHARGES</th>
								<c:forEach var="item" items="${entry.value}">
									<th>${ifn:cleanHtml(item)}</th>
									<input type="hidden" name="bedTypes" value="${ifn:cleanHtmlAttribute(item)}">
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'dailyChgarge'}">
							<tr>
								<td>Daily Charge</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" value="${ifn:afmt(item)}"
										onkeypress="return enterNumOnlyzeroToNine(event)"
										name="dailyCharge" class="number" maxlength="7"/></td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'minCharge'}">
							<tr>
								<td>Min Charge (Hourly)</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" value="${ifn:afmt(item)}"
										onkeypress="return enterNumOnlyzeroToNine(event)"
										name="minCharge" class="number" maxlength="7"/></td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'incrCharge'}">
							<tr>
								<td>Incr Charge(Hourly)</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" value="${ifn:afmt(item)}"
										onkeypress="return enterNumOnlyzeroToNine(event)"
										name="incrCharge" class="number" maxlength="7"/></td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'tax'}">
							<tr>
								<td>Tax(%)</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" value="${ifn:afmt(item)}"
										onkeypress="return enterNumOnlyzeroToNine(event)"
										name="tax" class="number" maxlength="7"/></td>
								</c:forEach>
							</tr>
						</c:when>
					</c:choose>
				</c:forEach>
			</table>
			</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
		</tr>

		<tr>
			<td colspan="4" align="center"><input type="button" name="Save"
				value="Save" onclick="validateSubmit();"/></td>
		</tr>

	</table>
</html:form>
</body>
</html>
