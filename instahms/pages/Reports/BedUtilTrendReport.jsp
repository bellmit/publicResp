<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Bed Utilization Trends - Insta HMS</title>
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
		padding-right: 5px;
		font-weight: bold;
	}

	table.detailFormTable1 {
	border-collapse: collapse;
	}

	table.detailFormTable1 td {
	white-space: nowrap;
	border: 1px solid silver;
	}
	table.detailFormTable1 th {
	white-space: nowrap;
	border: 1px solid silver;
	}
</style>
</head>
<body style="font:  10pt Courier New;" >
<c:if test="${not empty hospitaladdress}">

</c:if>
		<table width="100%"><tr><td ><div align="center" style="margin: 20px 2px 30px 2px;">
		<b><u>Bed Utilization Trend Report</u></b>
		</div></td></tr></table>

<c:if test="${not empty bedutilList}" >

<table  width="100%" cellspacing="10" cellpadding="10" id="table1" class="detailFormTable1">
		<%	String[] months={"01","02","03","04","05","06","07","08","09","10","11","12"};
		 	String[] monthNames={"Jan'","Feb'","Mar'","Apr'","May'","Jun'","July'","Aug'","Sep'","Oct'","Nov'","Dec'"};
		 	String[] days = {"31","28","31","30","31","30","31","31","30","31","30","31"};
		 	request.setAttribute("months",months);
		 	request.setAttribute("monthNames",monthNames);
		 	request.setAttribute("days",days);
		 %>
		<tr>
			<th align="left" style="width :1em">&nbsp;</th>
			<th  style="width :2em">&nbsp;</th>
			<th  style="width :1em">Days</th>


		<c:forEach var="mons" items="${monthsList}">
			<c:forEach var="mon" items="${mons}">
				<c:forEach var="month" items="${months}" varStatus="sts">
					<c:if test="${month == mon}"><td align="right" style="width :1em">${days[sts.index]}</td></c:if>
				</c:forEach>
		 	</c:forEach>
		  </c:forEach>

		</tr>

		<tr style="height: 3em">
			<th align="left" style="width :1em">Ward&nbsp;Name/<br/>Bed&nbsp;Type</th>
			<th  style="width :2em">No.&nbsp;Of<br/>beds</th>
			<th align="center" style="width :1em">Particulars</th>

		 	 <c:set var="year" value="${startYear}" />
		 	 <c:forEach var="mons" items="${monthsList}">
		 	 <c:forEach var="mon" items="${mons}">
				<c:forEach var="month" items="${months}" varStatus="sts">
					<c:if test="${month == mon}"><th>${months[sts.index] }/${fn:substring(year ,2,4)}</th></c:if>
				</c:forEach>
		 	 </c:forEach>
		 	 <c:set var="year" value="${year + 1}" />
		 	 </c:forEach>
		</tr>
		<c:forEach items="${bedutilList}" var="dtoList" varStatus="status">
		<tr>
			<td align="left">${dtoList.WARD}</td>
			<td style="width :1em">
				<c:forEach var="no" items="${noOfBeds}" >
					<c:if test="${no.BED_TYPE == dtoList.BED_TYPE && no.WARD_NO == dtoList.WARD_NO}">${no.NOOFBEDS}</c:if>
				</c:forEach>
			</td>
			<th  align="left">No.&nbsp;Of&nbsp;Patients</th>
			<c:set var="year" value="${startYear}" />
			<c:forEach var="mons" items="${monthsList}">
			<c:forEach var="mont" items="${mons}">
				<c:set var="coun" value="COUNT${mont}${year }" />
				<td  align="right"> ${ifn:cleanHtml(dtoList[coun])} </td>
			</c:forEach>
			<c:set var="year" value="${year + 1}" />
			</c:forEach>
		</tr>

		<tr>
			<th>&nbsp;</th>
			<th>&nbsp;</th>
			<th align="left">Billed&nbsp;Days</th>
			<c:set var="year" value="${startYear}" />
			<c:forEach var="mons" items="${monthsList}">
			<c:forEach var="mont" items="${mons}">
				<c:set var="billedDays" value="DAYS${mont}${year }" />
				<td  align="right"> ${ifn:cleanHtml(dtoList[billedDays])} </td>
			</c:forEach>
			<c:set var="year" value="${year + 1}" />
			</c:forEach>
		</tr>
		<tr>
			<th>&nbsp;</th>
			<th>&nbsp;</th>
			<th align="left">Billed&nbsp;Revenue</th>
			<c:set var="year" value="${startYear}" />
			<c:forEach var="mons" items="${monthsList}">
			<c:forEach var="mont" items="${mons}">
				<c:set var="amount" value="AMOUNT${mont}${year }" />
				<td  align="right"> <fmt:formatNumber maxFractionDigits="2">${ifn:cleanHtml(dtoList[amount])}</fmt:formatNumber> </td>
			</c:forEach>
			<c:set var="year" value="${year + 1}" />
			</c:forEach>
		</tr>
		<tr>
			<th>&nbsp;</th>
			<th>&nbsp;</th>
			<th align="left">Occupancy&nbsp;%</th>
			<c:set var="year" value="${startYear}" />
			<c:forEach var="mons" items="${monthsList}">
			<c:forEach var="mont" items="${mons}">
				<c:set var="billedDays" value="DAYS${mont}${year }" />

				<c:set var="totalDays">
					<c:forEach var="mon" items="${months}" varStatus="sts">
						<c:if test="${mont == mon}">${days[sts.index]}</c:if>
					</c:forEach>
				</c:set>

				<c:set var="occupancy">
					<c:forEach var="no" items="${noOfBeds}" >
						<c:if test="${no.BED_TYPE == dtoList.BED_TYPE && no.WARD_NO == dtoList.WARD_NO}">
							<fmt:formatNumber maxFractionDigits="2">  ${(dtoList[billedDays]*100)/(no.NOOFBEDS*totalDays)}	</fmt:formatNumber>
						</c:if>
					</c:forEach>
				</c:set>
				<td  align="right">${occupancy}</td>
			</c:forEach>
			<c:set var="year" value="${year + 1}" />
			</c:forEach>
		</tr>


		<tr>
			<th>&nbsp;</th>
			<th>&nbsp;</th>
			<th align="left">Monthly&nbsp;Average<br/>Revenue/Bed</th>

			<c:set var="total" value="0" />
			<c:set var="avgRevenue">
			<c:set var="year" value="${startYear}" />
			<c:forEach var="mons" items="${monthsList}" varStatus="sts">
			<c:forEach var="mont" items="${mons}">
				<c:set var="amount" value="AMOUNT${mont}${year }" />
				<c:set var="total">
					${ifn:cleanHtml(dtoList[amount] + total)}
				</c:set>
			</c:forEach>
			<c:set var="year" value="${year + 1}" />
			</c:forEach>
			<bean:size id="noOfMonths" name="monthsList"/>
			${total/monCount }
			</c:set>
			<c:forEach var="no" items="${noOfBeds}" >
				<c:if test="${no.BED_TYPE == dtoList.BED_TYPE && no.WARD_NO == dtoList.WARD_NO}">
				<td  align="right" colspan="${monCount }"><fmt:formatNumber maxFractionDigits="2">${avgRevenue/no.NOOFBEDS}</fmt:formatNumber></td>
				</c:if>
			</c:forEach>
		</tr>


		<tr>
			<th>&nbsp;</th>
			<th>&nbsp;</th>
			<th align="left">Monthly&nbsp;Average<br/>Occupancy</th>
			<c:set var="totalOcc" value="0" />
			<c:set var="avgoccupancy">
			<c:set var="year" value="${startYear}" />
			<c:forEach var="mons" items="${monthsList}">
			<c:forEach var="mont" items="${mons}">
				<c:set var="billedDays" value="DAYS${mont}${year }" />

				<c:set var="totalDays">
					<c:forEach var="mon" items="${months}" varStatus="sts">
						<c:if test="${mont == mon}">${days[sts.index]}</c:if>
					</c:forEach>
				</c:set>
				<c:set var="occupancy1">
					<c:forEach var="no" items="${noOfBeds}" >
						<c:if test="${no.BED_TYPE == dtoList.BED_TYPE && no.WARD_NO == dtoList.WARD_NO}">
							<fmt:formatNumber maxFractionDigits="2"> ${(dtoList[billedDays]*100)/(no.NOOFBEDS*totalDays)}	</fmt:formatNumber>
						</c:if>
					</c:forEach>
				</c:set>
				<c:set var="totalOcc">
					${totalOcc+occupancy1}
				</c:set>
			</c:forEach>
			<c:set var="year" value="${year + 1}" />
			</c:forEach>
			<bean:size id="noOfMonths" name="monthsList"/>
			${ totalOcc / monCount }
			</c:set>
			<td  align="right" colspan="${monCount }"><fmt:formatNumber maxFractionDigits="2">${avgoccupancy}</fmt:formatNumber></td>
		</tr>
		</c:forEach>




		<tr height="20px"></tr>

		<tr>
			<th>&nbsp;</th>
			<th>&nbsp;</th>
			<th align="left">Total&nbsp;Revenue</th>
			<c:set var="year" value="${startYear}" />
			<c:forEach var="mons" items="${monthsList}" varStatus="sts">
			<c:forEach var="mont" items="${mons}">
				<c:set var="amount" value="AMOUNT${mont}${year }" />
				<c:set var="total" value="0" />
				<c:set var="totRevenue">
				<c:forEach var="bedItem" items="${bedutilList}">
					<c:set var="total">
						${bedItem[amount] + total}
					</c:set>
				</c:forEach>
				${total}
				</c:set>
				<td align="right">${total}</td>
			</c:forEach>
			<c:set var="year" value="${year + 1}" />
			</c:forEach>

		</tr>

		<tr height="20px"></tr>

		<tr>
			<th>&nbsp;</th>
			<th>&nbsp;</th>
			<th align="left">Total&nbsp;Revenue/Bed</th>

			<c:set var="totalBeds" value="0"></c:set>
			<c:forEach var="no" items="${noOfBeds}" >
			<c:forEach var="bedItem" items="${bedutilList}">
						<c:if test="${no.BED_TYPE == bedItem.BED_TYPE && no.WARD_NO == bedItem.WARD_NO}">
						<c:set var="totalBeds">
							 ${no.NOOFBEDS + totalBeds}
							 </c:set>
						</c:if>
			</c:forEach>
			</c:forEach>

			<c:set var="year" value="${startYear}" />
			<c:forEach var="mons" items="${monthsList}" varStatus="sts">
			<c:forEach var="mont" items="${mons}">
				<c:set var="amount" value="AMOUNT${mont}${year }" />
				<c:set var="total" value="0" />
				<c:set var="totRevenue">
				<c:forEach var="bedItem" items="${bedutilList}">
					<c:set var="total">
						${bedItem[amount] + total}
					</c:set>
				</c:forEach>
				${total}
				</c:set>
				<td align="right"><fmt:formatNumber maxFractionDigits="2">${total / totalBeds}</fmt:formatNumber></td>
			</c:forEach>
			<c:set var="year" value="${year + 1}" />
			</c:forEach>
		</tr>
	</table>
</c:if>

<c:if test="${ empty bedutilList}" >
	<table  width="100%"><tr><td colspan="4" align="center">No Records Found</td></tr></table>
	</c:if>



</body>
</html>
