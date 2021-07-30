<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Bed Utilization Report - Insta HMS</title>




<script>
function initDisplay(){
	var alltags=document.all? document.all : document.getElementsByTagName("*");
	ccollect=getElementbyClass(alltags, "switchcontent");
	for ( i =0; i< ccollect.length; i++){
			ccollect[i].style.display="block";
		}
}
function setIds(){
	if(document.getElementById("prescriptiontable") != null){
		var prestablerows = document.getElementById("prescriptiontable").rows.length;
		var checkboxes = document.forms[0].cancel;
		for(var i = 0; i<checkboxes.length;i++){
			if(checkboxes[i].checked){
			checkboxes[i].value = checkboxes[i].id+'-'+checkboxes[i].value;
			}
		}
	}

}
function printbiew(){
	document.forms[0].action="bedutil.do";
	document.forms[0].method="bedutil";
	document.forms[0].submit();
}
</script>

<style type="text/css">
	table#patientDetails td.info {
		padding-right: 50px;
		font-weight: bold;
	}

	table.detailFormTable1 {
	font-size:12pt;
	border-collapse: collapse;
}

table.detailFormTable td {
	white-space: nowrap;
	border: 1px solid silver;
}


</style>
</head>


<body style="width: 720px;" >

<c:if test="${not empty hospitaladdress}">
	<table cellspacing="0" cellpadding="0" width="100%">
		<tr>
			<c:choose><c:when test="${not empty hospitaladdress.HLINE1}">
				<td width="33%">
					<img src="${pageContext.request.contextPath}/pages/Enquiry/report.do?method=getImage"
							width="110" alt=" ">
				</td>
				<td align="center" width="33%" style="white-space: nowrap">
					<b><font size="4">${hospitaladdress.HLINE1}</font></b>
					<br/>${hospitaladdress.HLINE2}
					<br/>${hospitaladdress.HLINE3}
					<br/>${hospitaladdress.HLINE4}
				</td>
				<td width="33%"></td>
			</c:when><c:otherwise>
				<td width="100%">
					<img src="${pageContext.request.contextPath}/pages/Enquiry/report.do?method=getImage"
						width="640" alt=" ">
				</td>
			</c:otherwise></c:choose>
		</tr>
	</table>
</c:if>


<table  width="100%" cellspacing="0" cellpadding="0" id="table1" class="detailFormTable1">
<tr><td colspan="11"><div align="center" style="margin: 20px 2px 30px 2px;">
		<b><u>Bed Utilization Report</u></b>
		</div></td></tr>

<tr><td colspan="11" align="center">From Date ${fromDate}&nbsp;&nbsp;&nbsp;To Date ${toDate}</td></tr>
		<tr><td colspan="11"><hr /></td></tr>

		<tr  style="width: 100%">
			<th>Ward&nbsp;Name/Bed&nbsp;Type</th>
			<th>&nbsp;&nbsp;&nbsp;&nbsp;</th>
			<th>No.&nbsp;Of&nbsp;Patients</th>
			<th>&nbsp;&nbsp;&nbsp;&nbsp;</th>
		 	<th>Billed&nbsp;Days</th>
		 	<th>&nbsp;&nbsp;&nbsp;&nbsp;</th>
		 	<th>Billed&nbsp;Revenue</th>
		 	<th>&nbsp;&nbsp;&nbsp;&nbsp;</th>
		 	<th>Occupancy&nbsp;%</th>
		 	<th>&nbsp;&nbsp;&nbsp;&nbsp;</th>
		 	<th>Average&nbsp;Revenue</th>
		</tr>

		<tr><td colspan="11"><hr /></td></tr>
		<c:forEach items="${bedutilList}" var="dtoList" varStatus="status">
			<c:set var="i" value="${status.index + 1}"/>
		<tr>
		<td >${dtoList.WARD}</td>
		<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<td align="center">${dtoList.COUNT}</td>
		<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<td align="center">${dtoList.DAYS}</td>
		<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<td align="right">${dtoList.AMOUNT}</td>
		<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<c:forEach var="no" items="${noOfBeds}" >
			<c:if test="${no.BED_TYPE == dtoList.BED_TYPE && no.WARD_NO == dtoList.WARD_NO}">
					<td align="right"><fmt:formatNumber maxFractionDigits="2">${(dtoList.DAYS*100)/(no.NOOFBEDS*totalDays)}</fmt:formatNumber></td>
			</c:if>
		</c:forEach>
		<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<c:forEach var="no" items="${noOfBeds}" >
			<c:if test="${no.BED_TYPE == dtoList.BED_TYPE && no.WARD_NO == dtoList.WARD_NO}">
					<td align="right"><fmt:formatNumber minFractionDigits="1" maxFractionDigits="2">${dtoList.AMOUNT/no.NOOFBEDS}</fmt:formatNumber></td>
			</c:if>
		</c:forEach>
		</tr>
		<tr><td colspan="11">&nbsp;</td></tr>
		</c:forEach>
	</table>



</body>
</html>
