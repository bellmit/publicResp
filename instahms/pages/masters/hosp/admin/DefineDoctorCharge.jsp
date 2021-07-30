<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html:html>
<head>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/Adddoctor.js" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insta HMS</title>
</head>
<body>
<html:form action="/pages/masters/hosp/admin/doctor.do">
	<%-- all form fields are populated automatically  --%>
	<html:hidden property="orgId" />
	<input type="hidden" name="method" value="${Chargemethod}" />
	<input type="hidden" name="charge_type" value="${ifn:cleanHtmlAttribute(param.charge_type)}" />
	<input type="hidden" name="docID" value="${ifn:cleanHtmlAttribute(param.doctorId)}" />
	<input type="hidden" name="contextPath" value="${pageContext.request.contextPath}" />
	<input type="hidden" name="dept_id" value="${ifn:cleanHtmlAttribute(param.deptFilter)}" />

	<html:hidden property="charge_type" />
	<html:hidden property="doctId" />
	<table class="formtable" align="center">
		<tr>
			<td colspan="4" align="center"><b>Doctor Charges</b></td>
		</tr>

		<tr>
			<td colspan="4">&nbsp;</td>
		</tr>

		<tr>
			<td>Doctor Name</td>
			<td><html:text property="doctorname"
				onblur="upperCase(doctorname)" styleId="doctorname" readonly="true" /></td>
			<td>Department</td>
			<td><html:text property="deptName" readonly="true"></html:text>
			</td>
		</tr>

		<tr>
			<td>Rates for organization:</td>
			<td>
					<insta:selectdb name="ratePlan" value="${param.orgId}"
							table="organization_details" valuecol="org_id" orderby="org_name"
							displaycol="org_name" onchange="changeRatePlan();"/>
			</td>
			<td>OP Validity Days</td>
			<td><html:text property="validity"
				onkeypress='return enterNumOnlyzeroToNine(event)'
				styleClass="number"  readonly="true"/></td>
		</tr>

		<tr>
			<td>OP Consultation Charge</td>
			<td><html:text property="doctorOPCharge" styleClass="number" /></td>
			<td>OP Revisit Charge</td>
			<td><html:text property="OPRevistCharge" styleClass="number" /></td>
		</tr>

		<tr>
			<td>Private OP Consultation Charge</td>
			<td><html:text property="doctorPrivOPCharge" styleClass="number" /></td>
			<td>Private OP Revisit Charge</td>
			<td><html:text property="privOPRevistCharge" styleClass="number" /></td>
		</tr>


		<tr>
			<td colspan="4">&nbsp;</td>
		</tr>

		<c:set var="rowCount" value="1" />
		<c:set var="colCount" value="1" />

		<tr>
			<td colspan="4">
			<table class="dashboard" align="center">
				<c:forEach var="entry" items="${requestScope.outputTable}">
					<c:choose>
						<c:when test="${entry.key eq 'CHARGES'}">
							<tr>
								<th>CHARGES</th>
								<c:forEach var="item" items="${entry.value}">
									<th>${ifn:cleanHtml(item)}</th>
									<input type="hidden" name="bedtype" value="${ifn:cleanHtmlAttribute(item)}">
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'IPCHARGE'}">
							<tr>
								<td>IP Consultation</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" name="ipCharge"
										value="${ifn:afmt(item)}" size="3" class="number"
										onkeypress="return enterNumOnlyzeroToNine(event)"></td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'NIGHTCHARGE'}">
							<tr>
								<td>Night Consultation</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" name="nightCharge"
										value="${ifn:afmt(item)}" size="3" class="number"
										onkeypress="return enterNumOnlyzeroToNine(event)"></td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'SURGEONCHARGE'}">
							<tr>
								<td>${SUREGEON}</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" name="otCharge" class="number"
									onkeypress="return enterNumOnlyzeroToNine(event)"	value="${ifn:afmt(item)}" size="3"></td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'ASSTSURGEON'}">
							<tr>
								<td>${ASSTSURGEON}</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" name="assOTCharge" class="number"
									onkeypress="return enterNumOnlyzeroToNine(event)"	value="${ifn:afmt(item)}" size="3"></td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'COSURGEON'}">
							<tr>
								<td>${COSURGEON}</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" name="co_OpSurgeonCharge" class="number"
									onkeypress="return enterNumOnlyzeroToNine(event)"	value="${ifn:afmt(item)}" size="3"></td>
								</c:forEach>
							</tr>
						</c:when>

					</c:choose>
				</c:forEach>
			</table>
			</td>
		</tr>

		<tr>
			<td colspan="4">&nbsp;</td>
		</tr>

		<tr>
			<td align="center" colspan="4"><html:submit value="Save"
				property="Save" /></td>
		</tr>
	</table>
</html:form>

</body>
</html:html>
