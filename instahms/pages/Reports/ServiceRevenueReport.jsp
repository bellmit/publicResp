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
<title>Services Revenue Report - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">


<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="/reports/servicerevenuereport.js"/>

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
								<td>
										<table align="center">

											<tr>
												<td colspan="2">Select a date range (or select From and To dates manually)</td>
											</tr>
											<tr>
												<td valign="top">
													<input checked type="radio" id="pd" name="_sel" onclick="setDateRangeYesterday(fdate, todate)">
													<label for="pd">Yesterday</label>
													<br/>
													<input type="radio" id="td" name="_sel" checked onclick="setDateRangeToday(fdate, todate)">
													<label for="td">Today</label>
													<br/>
													<input type="radio" id="pm" name="_sel" onclick="setDateRangePreviousMonth(fdate, todate)">
													<label for="pm">Previous Month</label>
													<br/>
													<input type="radio" id="tm" name="_sel" onclick="setDateRangeMonth(fdate, todate)">
													<label for="tm">This Month</label>
													<br/>
													<input type="radio" id="pfy" name="_sel" onclick="setDateRangePreviousFinancialYear(fdate, todate)">
													<label for="pfy">Previous Financial Year</label>
													<br/>
													<input type="radio" id="tfy" name="_sel" onclick="setDateRangeFinancialYear(fdate, todate)">
													<label for="tfy">This Financial Year</label>
													<br/>
												</td>
												<td valign="top" style="padding-left: 2em">
													<table>
														<tr>
															<td align="right">From:</td>
															<td><insta:datewidget name="fdate" value="today"/></td>
														</tr>
														<tr>
															<td align="right">To:</td>
															<td><insta:datewidget name="todate" value="today"/></td>
														</tr>
													</table>
												</td>
											</tr>
									</table>
								</td>
							</tr>
							<tr>
								<td>
								</td>
							</tr>
							<tr>
								<td>
								</td>
							</tr>
							<c:if test="${names != 'Patient Discount Report'}">
							<tr>
								<td >&nbsp;&nbsp;&nbsp;Select department Name  :&nbsp;&nbsp;All
									<input type="checkbox" name="allDept" checked="checked" onclick="selectordeselectAll();">
								</td>
							</tr>
							<tr>
								<td colspan="2" align="center">
									<select name="servdept" multiple="multiple"  size="5" id="servdept" onclick="deselectAll();">
										<c:forEach var="sdept" items="${servdeptlist}">
											<option value="${sdept.DEPARTMENT}">${sdept.DEPARTMENT} </option>
										</c:forEach>
									</select>
								</td>
							</tr>
						</c:if>
						</table>

						<table align="center" style="margin-top: 1em">
							<tr>
								<td>
 									<button type="button" name="print" id="print" accesskey="P" class="button" onclick="getserviceRevenue();">
 									<b><u>P</u></b>rint</button>
       								<button type="button" name="exporttocsv" id="exporttocsv" accesskey="E" class="button" onclick="getServicerevenueExportReport();">
       								<b><u>E</u></b>xport to CSv</button>
								</td>
						</tr>
						</table>

</form>
<script>
	var names = '${requestScope.names}';
</script>
</body>
</html>
