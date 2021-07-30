<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>
<html>
<head>
<title>ICD code wise Diagnosis Report - Insta HMS</title>

	<script>


		var fromDate, toDate,category ;

		function onInit() {
			fromDate = document.inputform.fromDate;
			toDate = document.inputform.toDate;
			document.getElementById("pd").checked = true;
			setDateRangeYesterday(fromDate, toDate);
		}

			function validateForm(){

				if (validateFromToDate(document.inputform.fromDate,document.inputform.toDate)){
					return true;
				}
				return false;
		}

	</script>
</head>
	<body onload="onInit();">
		<div class="pageHeader">ICD code wise Diagnosis Report</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="method" value="getReport">

			<div class="tipText">

			</div>

			<table align="center">
				<tr>
					<td  align="left">
						<jsp:include page="/pages/Common/DateRangeSelector.jsp">
							<jsp:param name="skipWeek" value="Y"/>
						</jsp:include>
					</td>
				</tr>
				<c:choose>
					<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
						<tr style="align:center;">
							<td style="padding-left: 64px"><br />Center:&nbsp;
								<insta:selectdb name="center" id="center" table="hospital_center_master" valuecol="center_id" displaycol="center_name" value="${centerId}" />
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

