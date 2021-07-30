<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="org.apache.struts.Globals"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html:html>
<head>
<title>Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/Adddoctor.js" />
<insta:link type="js" file="ajax.js" />
</head>
<body onload="keepbackupVariable()">
<html:form method="POST" action="/pages/masters/hosp/admin/doctor.do"
	onsubmit="return saveDetails();">
	<input type="hidden"
		name="<%=org.apache.struts.taglib.html.Constants.TOKEN_KEY%>"
		value="${Globals.TRANSACTION_TOKEN_KEY}<%-- <bean:write name="<%=Globals.TRANSACTION_TOKEN_KEY%>"/> --%>">
	<html:hidden property="method" value="${methodName}" />
	<html:hidden property="doctorId" value="${doctorId}"/>
	<html:hidden property="deptName" />
	<input type="hidden" id="paymentCategory" value="${paymentCategory}">
	<input type="hidden" name="contextPath" value="${pageContext.request.contextPath}"/>
	<table class="formtable" align="center">
	<c:if test="${methodName == 'editDoctorDetails'}">
		<tr>
			<th colspan="6" align="center">Edit Doctor Details</th>
		</tr>
	</c:if>
	<c:if test="${methodName != 'editDoctorDetails'}">
		<tr>
			<th colspan="6" align="center">Add New Doctor </th>
		</tr>

	</c:if>

		<tr>
			<td><b>${requestScope.successmsg}&nbsp;</b></td>
		</tr>

		<tr>
			<td>Doctor Name:(<b>DR.</b>)</td>
			<td><html:text property="doctorname"
				onblur="checkDuplicate()" styleId="doctorname" maxlength="100"/></td>
			<td>Department:</td>
			<td><html:select property="deptid">
				<html:option value="">--Department--</html:option>
				<c:forEach var="item" items="${requestScope.departments}">
					<html:option value="${item.DEPT_ID}">${item.DEPT_NAME} </html:option>
				</c:forEach>
			</html:select></td>
			<td>Doctor Type:</td>
			<td><html:select property="doctortype" onchange="setPaymentEligible();">
				<html:option value="">--Doctor Type--</html:option>
				<html:option value="HOSPITAL">HOSPITAL</html:option>
				<html:option value="CONSULTANT">CONSULTANT</html:option>
			</html:select></td>
		</tr>

		<tr>
			<td>Qualification:</td>
			<td><html:text property="qualification" maxlength="50"/></td>
			<td>Specialization:</td>
			<td><html:text property="specialization"
				onblur="upperCase(specialization)" styleId="specialization" size="15" maxlength="50"/></td>

			<td>Registration Number:</td>
			<td><html:text property="regNumber" size="15" maxlength="15"/></td>
		</tr>
		<tr>
			<td>Clinic Phone:</td>
			<td><html:text property="clinicPhone"  maxlength="15" onkeypress="return enterPhone(event)"/></td>
			<td>Mobile:</td>
			<td><html:text property="phonenumber" size="15" maxlength="15" onkeypress="return enterPhone(event)"/></td>
			<td>Res.Phone:</td>
			<td><html:text property="resPhone" size="15" maxlength="15" onkeypress="return enterPhone(event)"/></td>
	   </tr>
		<tr>
			<td>Email:</td>
			<td><html:text property="email"  maxlength="50"/></td>
			<td>Doctor License Number:</td>
			<td><html:text property="licenseNumber" size="15"></html:text></td>
			<td>Address:</td>
			<td><html:textarea rows="2" property="doctorAddress" onkeypress="return addrValidation();">
			</html:textarea></td>
		</tr>

		<tr>

			<td>OT Doctor:</td>
			<td><html:select property="isOTDoctor">
				<html:option value="N">NO</html:option>
				<html:option value="Y">Yes</html:option>
			</html:select></td>
			<td>Status:</td>
			<td><html:select property="activeStatus">
				<html:option value="A">Active</html:option>
				<html:option value="I">InActive</html:option>
			</html:select></td>
			<td>Schedulable:</td>
			<td><html:checkbox property="schedulable"></html:checkbox></td>
		</tr>
		<tr>
			<td>Payment Category:</td>
			<td><html:select property="payCatId">
				<c:forEach var="item" items="${requestScope.docCategory}">
					<html:option value="${item.CAT_ID}">${item.CAT_NAME} </html:option>
				</c:forEach>
			</html:select></td>
			<td>Payment Eligible:</td>
			<td><html:select property="payEligible">
				<html:option value="N">No</html:option>
				<html:option value="Y">Yes</html:option>
			</html:select></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>Consultation Validity:</td>
			<td><html:text property="validity"
				onkeypress='return enterNumOnlyzeroToNine(event)' size="15"/></td>
			<td>Allowed Revisit Count:</td>
			<td><html:text property="allowed_revisit_count"
				onkeypress='return enterNumOnlyzeroToNine(event)' size="15"/></td>
		</tr>
		<tr>
			<td colspan="6" align="center"><html:submit value="Save" /></td>
		</tr>

	</table>
</html:form>
</body>
</html:html>
