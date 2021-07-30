<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>

<head>
	<title>OT Schedule Report - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<script>
		function init() {
			document.getElementById('pd').checked = true;
			setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
		}
		function validateFields() {
			if(!validateFromToDate(document.inputform.fromDate, document.inputform.toDate)) return false;
			if(document.inputform.surgeonArray.value == '' &&
				document.inputform.anesthetistArray.value == '' &&
				document.inputform.surgeryArray.value == '' &&
				document.inputform.anesthesiaTypeArray.value == '') {
				alert("Please select any filter option");
				return false;
			}
			return true;
		}
	</script>
</head>
<body onload="init();">
		<div class="pageHeader">Operation Schedule Report</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="method" value="getReport"/>

			<div class="tipText" align="center">
				This report lists Operations Scheduled  in the given time period.
			</div>

			<table align="center">
				<jsp:include page="/pages/Common/DateRangeSelector.jsp">
					<jsp:param name="addTable" value="N"/>
					<jsp:param name="skipWeek" value="Y"/>
				</jsp:include>
			</table>

			<center><br/>
			<label>Filter Options:</label>
			<div class="stwMain" style="width:60%;">
			<table width="100%" height="15%" class="search">
			<tr>
				<th>Surgeon Name</th>
				<th>Anesthetist</th>
				<th>Surgery</th>
				<th>Anesthesia Type</th>
			</tr>
			<tr>
				<td>
					<insta:selectdb  name="surgeonArray"  size = "5"
					multiple= "true" value="" table="doctors" valuecol="doctor_id"
					displaycol="doctor_name" filtercol="ot_doctor_flag" filtervalue="Y"/>
				</td>
				<td>
					<insta:selectdb  name="anesthetistArray"  size = "5"
					multiple= "true" value="" table="doctors" valuecol="doctor_id"
					displaycol="doctor_name" filtercol="dept_id" filtervalue="DEP0002"/>
				</td>
				<td>
					<insta:selectdb  name="surgeryArray"  size = "5"
					multiple= "true" value="" table="operation_master" valuecol="op_id"
					displaycol="operation_name"/>
				</td>
				<td>
					<insta:selectdb  name="anesthesiaTypeArray"  size = "5"
					multiple= "true" value="" table="anesthesia_master" valuecol="anesthesia_type"
					displaycol="anesthesia_type"/>
				</td>
			</tr>
			</table>
			</div>
			</center>

			<table align="center" style="margin-top: 1em">
			<tr>
				<td>
					<button type="submit" accesskey="G"
						onclick="return validateFields();"><b><u>G</u></b>enerate Report</button>
				</td>
			  </tr>
			</table>

		</form>
	</body>
</html>

