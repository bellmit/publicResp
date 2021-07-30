<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="org.apache.struts.Globals"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/Adddoctor.js" />
</head>
<body>
<html:form method="POST" action="/pages/masters/hosp/admin/doctor.do">
	<input type="hidden" name="method" value="updatePayments">
	<input type="hidden" name="doctId" value="${DocForm.doctId}"/>
	<table class="formtable" align="center">
		<tr>
			<th colspan="4" align="center">Doctor Payments</th>
		</tr>

		<tr>
			<th colspan="4">&nbsp;</th>
		</tr>
		<tr>
			<td>Doctor Name:</td>
			<td><b>${DocForm.doctorname}</b></td>
			<td>Department:</td>
			<td><b>${DocForm.deptName}</b></td>
		</tr>

		<tr>
			<td>PaymentType:</td>
			<td colspan="3"><html:radio property="paymentType" styleId="paymentTypePercentage" value="Y" >Percentage</html:radio>
			<html:radio property="paymentType" styleId="paymentTypeAmount" value="N">Amount</html:radio></td>
		</tr>

		<tr>
			<td>&nbsp;</td>
		</tr>

		<tr>
			<td colspan="4">
			<table class="dashboard">
				<tr>
					<th>Charge</th>
					<th>Doctor Payment</th>
				</tr>

				<tr>
					<td>OP Charges</td>
					<td><html:text property="docPayForOP" styleClass="number" value="${ifn:afmt(DocForm.docPayForOP)}"  /></td>
				</tr>

				<tr>
					<td>IP Charges(Except Operations)</td>
					<td><html:text property="docPayForIP" styleClass="number" value="${ifn:afmt(DocForm.docPayForIP)}" /></td>
				</tr>

				<tr>
					<td>Operations Charges</td>
					<td><html:text property="docPayForOperation" styleClass="number" value="${ifn:afmt(DocForm.docPayForOperation)}" /></td>
				</tr>
			</table>
			</td>
		</tr>

		<tr>
			<td>&nbsp;</td>
		</tr>


		<tr>
			<td colspan="4" align="center">
				<input type="button" value="Save" onclick="submitPayments()"/>
			</td>
		</tr>

	</table>
</html:form>
</body>
</html>
