<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>IP/OP Stats Report - Insta HMS</title>

	<script>


		var fromDate, toDate,category ;

		function onInit() {
			fromDate = document.inputform.fromDate;
			toDate = document.inputform.toDate;
			setDateRangeYesterday(fromDate,toDate);
			document.getElementById('OPS').checked = true;
		}

			function validateForm(format){

					if (validateFromToDate(document.inputform.fromDate,document.inputform.toDate)) {
						if ( format == 'csv') {
							document.forms[0].method.value = 'getCsv';
							return true;
						}else {
							document.forms[0].method.value = 'getReport';
							return true;

						}
					}
				return false;
		}

	</script>
</head>
	<body onload="onInit();">
		<div class="pageHeader">IP/OP Statistics Report</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="method" value="getReport">

			<div class="tipText">

			</div>

			<table align="center">
				<jsp:include page="/pages/Common/DateRangeSelector.jsp">
					<jsp:param name="addTable" value="N"/>
					<jsp:param name="skipWeek" value="Y"/>
				</jsp:include>
				<tr height="10px"><td></td></tr>

				<tr>
					<td>

						<input type="radio" name="report" value="ops" id="OPS"/>OP Statistics Report - Consultant wise with Gender break up <br/>
						<input type="radio" name="report" value="opsd" id="OPSD"/>OP Statistics Report - Dept wise with Gender break up <br/>
						<input type="radio" name="report" value="ipsd" id="ipsd"/>IP Statistics Report - Dept wise <br/>
						<input type="radio" name="report" value="ipsdg" id="ipsdg"/>IP Statistics Report - Dept wise with Gender break up<br/>
						<input type="radio" name="report" value="ipsc" id="ipsc"/>IP Statistics Report - Consultant wise <br/>
						<input type="radio" name="report" value="ipscg" id="ipscg"/>IP Statistics Report - Consultant wise with Gender break up <br/>
						<input type="radio" name="report" value="ipsdabo" id="ipsdabo"/>IP Stats Dept wise with ABO

					</td>
				</tr>
				<c:choose>
					<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
						<tr>
							<td align="right"><br />Center:&nbsp;
								<insta:selectdb name="center" id="center" table="hospital_center_master" valuecol="center_id" displaycol="center_name" value="${centerId}"/>
							</td>
						</tr>
					</c:when>
					<c:otherwise>
							<input type="hidden"  name="center"  id="center" value="${centerId}"/>
					</c:otherwise>
				</c:choose>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
						<button type="submit" accesskey="G"
						onclick="return validateForm()"><b><u>G</u></b>enerate Report</button>
					</td>
				</tr>
			</table>

		</form>
	</body>
</html>

