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
<insta:link type="js" file="ajax.js" />
</head>
<body onload="keepbackupVarible()">
<html:form action="/pages/masters/insta/operationtheatre/opmast.do">
	<input type="hidden" name="contextPath" value="${pageContext.request.contextPath}" />
	<html:hidden property="orgId" />
	<input type="hidden" name="method" value="${requestScope.method}" />
	<html:hidden property="operationId" value="${OPerMasterForm.operationId}" />
	<html:hidden property="chargeType" value="${OPerMasterForm.chargeType}"/>
	<html:hidden property="pageNum" />


	<table class="formtable" align="center">
		<tr>
			<th align="center" colspan="6">Operations Details Screen</th>
		</tr>

		<tr>
			<th>&nbsp;</th>
		</tr>

		<tr>
			<td>Operation Name</td>
			<td><html:text property="operationName" styleId="operationName"
				onblur="checkDuplicate();capWords(operationName);" /></td>
			<td>Department</td>
			<td><html:select property="deptid">
				<html:option value="">--Department--</html:option>
				<c:forEach var="item" items="${requestScope.departments}">
					<html:option value="${item.DEPT_ID}">${item.DEPT_NAME} </html:option>
				</c:forEach>
			</html:select></td>
		</tr>

		<tr>

			<td>Status</td>
			<td><html:select property="activeStatus">
				<html:option value="A">Active</html:option>
				<html:option value="I">InActive</html:option>
			</html:select></td>
		</tr>

		<tr>
			<td>Rates For Rate Sheet</td>
			<td>
				<c:choose>
				<c:when test="${param.method ne 'getNewOperationScreen'}">
					<insta:selectdb name="ratePlan" value="${OPerMasterForm.orgId}"
							table="organization_details" valuecol="org_id" orderby="org_name"
							displaycol="org_name" onchange="chgRate();"
							filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
				</c:when>
				<c:otherwise>
					<html:text property="orgName" readonly="true" />
				</c:otherwise>
				</c:choose>
			</td>
		</tr>

		<tr>
			<td>Rate Plan Code :</td>
			<td><html:text property="orgItemCode" maxlength="20"/></td>
			<td>Applicable for Rate Sheet :</td>
			<td><html:radio property="applicable" value="true" />Yes<html:radio property="applicable" value="false" />No</td>
		</tr>

		<tr>
			<td>&nbsp;</td>
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
						<c:when test="${entry.key eq 'Surgeon Charge'}">
							<tr>
								<td>Surgeon Charge</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" value="${ifn:afmt(item)}"
									onkeypress="return enterNumOnlyzeroToNine(event)" name="surgeonCharge" class="number" /></td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'Anesthetist Charge'}">
							<tr>
								<td>Anesthetist Charge</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" value="${ifn:afmt(item)}"
									onkeypress="return enterNumOnlyzeroToNine(event)" class="number" name="anesthetistCharge" /></td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'Surgical Assistance Charge'}">
							<tr>
								<td>Surgical Assistance Charge</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" value="${ifn:afmt(item)}"
									onkeypress="return enterNumOnlyzeroToNine(event)" class="number" name="surgicaAsstCharge" /></td>
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
				value="Save" onclick="validate();"/></td>
		</tr>
	</table>

</html:form>
</body>
</html>
