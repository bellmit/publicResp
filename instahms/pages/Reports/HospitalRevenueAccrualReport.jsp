<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>Accrual Based Revenue Sumary - Insta HMS</title>

<script>
	function onInit() {
		setDateRangeYesterday(document.inputform.fromDate,document.inputform.toDate);
	}

	function validateForm(format){
		document.inputform.format.value=format;
		if (validateFromToDate(document.inputform.fromDate, document.inputform.toDate)) {
				return true;
		}
		return false;
	}
</script>
</head>

<body onload="onInit();">
	<div class="pageHeader">Revenue Report</div>
	<form name="inputform" method="GET" target="_blank">
		<input type="hidden" name="_method" value="getReport">
		<input type="hidden" name="format" value="pdf">

		<div class="helpPanel">
			This report shows as summary of accrual based revenue.<br/><br/>
			<font color='red'>NOTE: We would recommend that you use the Bill Charge Adjustment Report Builder as revenue recognition is not dependent on Bill Finalization as was the case in previous releases and instead based on Accrual. This report is now not being maintained and would be removed soon. Do contact us at insta-support@practo.com if you need support or any more details.</font>
		</div>

		<jsp:include page="/pages/Common/DateRangeSelector.jsp">
			<jsp:param name="skipWeek" value="Y"/>
		</jsp:include>

		<div style="margin-top: 10px">
			<button type="submit" onclick="return validateForm('screen')">View</button>
			<button type="submit" onclick="return validateForm('pdf')">Print</button>
		</div>
	</form>
</body>
</html>

