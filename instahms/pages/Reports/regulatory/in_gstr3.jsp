<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>GSTR3 Report- Insta HMS</title>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<c:set var="valueDate">
	<fmt:formatDate pattern="dd-MM-yyyy  HH:mm" value="${currentDate}"/>
</c:set>
<script>
	function onInit() {
		document.getElementById('pd').checked = true;
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
	<div class="pageHeader">India GSTR3 Report</div>
	<form name="inputform" method="GET" target="_blank">
		<input type="hidden" name="_method" value="getReport">
		<input type="hidden" name="format" value="pdf">
		<input type="hidden"  name="currDateTime"  id="currDateTime" value="${valueDate}"/>

		<div class="helpPanel">
			The GSTR 3 Report (contains 15 headings) and is mostly auto-populated by fetching data from the GSTR-1 and GSTR-2 and hence this report only facilitates the data to reduce the time to fill/confirm additional details.
		</div>

		<jsp:include page="/pages/Common/DateRangeSelector.jsp">
			<jsp:param name="skipWeek" value="Y"/>
		</jsp:include>
		<br/><br/>
				<table>
					<tr>
						<td align="right">Store:</td>
						<td>
							<insta:selectdb name="dept_name" id="dept_name" table="stores" valuecol="dept_name" displaycol="dept_name"   dummyvalue="-- Select --"/>
						</td>
					</tr>
				</table>
		<br/>
		<div style="margin-top: 10px">
			<button type="submit" onclick="return validateForm('screen')">View</button>
			<button type="submit" onclick="return validateForm('pdf')">Print</button>
		</div>
	</form>
</body>
</html>

