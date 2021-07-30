<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page isELIgnored="false"%>
<%
response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-store");
response.setHeader("Expires", "0");
%>
<html>
<head>
<title>Lab Revenue Report - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">


<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="/reports/diagrevreport.js"/>

</head>
<body onload="init();">
	<input type="hidden" name="pageName" value="${requestScope.names}"/>
	<div class="pageHeader">
			${requestScope.names}
	</div>
<form method="POST" ><!-- Main tab-->

       <div class="tipText" style="width:50em">
			${requestScope.note}
	</div>

<!-- main table  start -->
						<table align="center">
							<tr>
								<td colspan="2">Select a date range for the report</td>
							</tr>
							<tr>
								<td valign="top">
									<input checked type="radio" id="pd" name="condition" onclick="setDateRangeYesterday(document.forms[0].fdate,document.forms[0].todate)">
									<label for="pd">Yesterday</label>
									<br/>

									<input type="radio" id="td" name="condition" checked onclick="setDateRangeToday(document.forms[0].fdate, document.forms[0].todate)">
									<label for="td">Today</label>
									<br/>

									<input type="radio" id="pm" name="condition" onclick="setDateRangePreviousMonth(document.forms[0].fdate, document.forms[0].todate)">
									<label for="pm">Previous Month</label>
									<br/>

									<input type="radio" id="tm" name="condition" onclick="setDateRangeMonth(document.forms[0].fdate, document.forms[0].todate)">
									<label for="tm">This Month</label>
									<br/>

									<input type="radio" id="pfy"
										name="condition" onclick="setDateRangePreviousFinancialYear(document.forms[0].fdate, document.forms[0].todate)">
									<label for="pfy">Previous Financial Year</label>
									<br/>

									<input type="radio" id="tfy" name="condition" onclick="setDateRangeFinancialYear(document.forms[0].fdate, document.forms[0].todate)">
									<label for="tfy">This Financial Year</label>
									<br/>
								</td>
								<td>
									<table>
										<tr>
											<td align="right">From :</td>
											<td><insta:datewidget name="fdate" value="today"/></td>
										</tr>
										<tr>
											<td align="right">To :</td>
											<td><insta:datewidget name="todate" value="today"/></td>
										</tr>
									</table>

								</td>
							</tr>
							<tr>
								<td>Select test type :</td>
								<td>
									&nbsp;&nbsp;All<input type="checkbox" name="allType" onclick="validateSampleType();">
									&nbsp;&nbsp;InComing<input type="checkbox" name="incomingTests" value="'i'" onclick="validateSampleType();">
									&nbsp;&nbsp;OutGoing<input type="checkbox" name="outGoing" value="'o'" onclick="validateSampleType();">
								</td>
							</tr>
							<c:if test="${names != 'Patient Discount Report'}">
							<tr>
								<td>Select department Name  :</td>
								<td>&nbsp;&nbsp;All
									<input type="checkbox" name="allDept" checked="checked" onclick="selectordeselectAll();">
								</td>
							</tr>
							<tr>
								<td colspan="2" align="center">
									<select name="diagdept" multiple="multiple"  size="5" id="diagdept" onclick="deselectAll();">
										<c:forEach var="ddept" items="${diagdeptlist}">
											<option value="${ddept.DDEPT_ID}">${ddept.DDEPT_NAME} </option>
										</c:forEach>
									</select>
								</td>
							</tr>
						</c:if>
						</table>

						<table align="center" style="margin-top: 1em">
							<tr>
								<td>
 									<button type="button" name="print" id="print" accesskey="P" class="button" onclick="getlabRevenue();">
 									<b><u>P</u></b>rint</button>
       								<button type="button" name="exporttocsv" id="exporttocsv" accesskey="E" class="button" onclick="getLabrevenueExportReport();">
       								<b><u>E</u></b>xport to CSV</button>
								</td>
						</tr>
						</table>

</form>
<script>
	var names = '${requestScope.names}';
</script>
</body>
</html>
