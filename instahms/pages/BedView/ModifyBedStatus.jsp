<%@page import="org.apache.struts.Globals"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>
<head>
	<title>Modify Bed Status- Insta HMS</title>

	<meta http-equiv="Content Type" content="text/html; charset=iso-8859-1">


	<insta:link type="script" file="date_go.js" />
	<insta:link type="script" file="BedView/modifybedstatus.js" />

	<c:url var="bedviewURL" value="/pages/ipservices/BedView.do?_method=getBedView"/>
</head>
<body class="yui-skin-sam" onload="init();">
	<jsp:useBean id="currentDate" class="java.util.Date"/>
	<fmt:formatDate var="dateVal" value="${currentDate}" pattern="dd-MM-yyyy"/>
	<fmt:formatDate var="timeVal" value="${currentDate}" pattern="HH:mm"/>

	<fmt:formatDate var="avblDate" value="${beddetails.map.avilable_date}" pattern="dd-MM-yyyy"/>
	<fmt:formatDate var="avblTime" value="${beddetails.map.avilable_date}" pattern="HH:mm"/>


	<form action="BedView.do" method="POST" name="bedstatusform">
		<input type="hidden" name="_method"value="modifyBedStatus"/>
		<insta:feedback-panel/>
		<h1>Modify Bed Status</h1>

		<table class="formtable">
			<tr>
				<td>
					<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Bed&nbsp;Details</legend>
						<table class="formtable">
							<tr>
								<td class="formlabel">Bed Name:</td>
								<input type="hidden" name="bed_id" value="${beddetails.map.bed_id}"/>
								<td class="forminfo">${beddetails.map.bed_name} </td>

								<td class="formlabel">Bed Type:</td>
								<td class="forminfo">${beddetails.map.bed_type} </td>

							</tr>
							<tr>
								<td class="formlabel">Ward Name:</td>
								<td class="forminfo">${beddetails.map.ward_name}
								</td>

								<td class="formlabel">Bed Status:</td>
								<c:set var="bed_status" value="Available"/>
								<c:if test="${beddetails.map.bed_status == 'C'}">
									<c:set var="bed_status" value="Under Cleaning"/>
								</c:if>
								<c:if test="${beddetails.map.bed_status == 'M'}">
									<c:set var="bed_status" value="Under Maintanance"/>
								</c:if>
								<td class="forminfo">${bed_status} </td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
			<tr>
				<td>
					<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Modify Bed&nbsp;Status</legend>
						<table>
							<tr>
								<td class="formlabel">Bed Status:</td>
								<td>
									<insta:selectoptions name="bed_status" opvalues="A,C,M" optexts="Available,Cleaning,Maintainance"
									value="${beddetails.map.bed_status}" onchange="showDateAndTime();"/>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Reason:</td>
								<td>
									<input type="text" value="${beddetails.map.remarks}" name="remarks" maxlength="50"/>
								</td>
							</tr>
							<tr id="avbleTimingsDiv" style="visibility: ${beddetails.map.bed_status == 'A'?'hidden':'visible'}">
									<td class="formlabel">Available Timing:</td>
									<td>
										<insta:datewidget name="avbl_date" id="avbl_date" btnPos="left" value="${avblDate != null?avblDate:dateVal}" />
										<input type="text" class="timefield" size="4" name="avbl_time" id="avbl_time" value="${avblTime != null?avblTime:timeVal }" />
									</td>
								</tr>
							<tr>
								<td>
									<input type="submit" value="Save" name="modify" onclick="return validate();"/>
									|<a href="${bedviewURL }" >Bed View</a>
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
	   </table>
	</form>
</body>
</html>