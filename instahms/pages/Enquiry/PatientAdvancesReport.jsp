<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>


<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>Advances Report - Insta HMS</title>

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

		function csvReport() {
			if ( (validateForm())){
				document.inputform.action="${cpath}/exportPatientAdvances.do";
				document.inputform.method.value = 'exportToCSV';
				document.inputform.submit();
			}else{
				return false;
			}
		}


		function pdfReport() {

			if ( (validateForm())){
				document.inputform.action="${cpath}/Enquiry/advancereport.do";
				document.inputform.method.value = 'getReport';
				document.inputform.submit();
			}else{
				return false;
			}
		}
	</script>
</head>
	<body onload="onInit();">
		<div class="pageHeader">Patient Advance</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="report" value="PatientAdvanceReport">
			<input type="hidden" name="method" value="getReport">

			<div class="tipText">
						This report gives detailed list of advances given by the  patients between the given dates .
                        The report displays MR no, Patient Visit Id, Patient Name, Bill Number, Advance Amount.
			</div>

			<table align="center">
				<tr>
					<td  align="left">
						<jsp:include page="/pages/Common/DateRangeSelector.jsp">
							<jsp:param name="skipWeek" value="Y"/>
						</jsp:include>
					</td>
				</tr>
			</table>

			<table align="center" style="margin-top: 1em">

			    <tr>
			        <td>
			        <label for="filterby">FilterBy:</label>
			          <select name="filterValue" id="filterValue">
							<option value="*" selected>...Select...</option>
							<option value="i">In Patient</option>
							<option value="o">Out Patient</option>
						</select>
				    <td>
			    </tr>
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
						<button type="submit" accesskey="G"
							onclick="return pdfReport();"><b><u>G</u></b>enerate Report</button>
						<button type="submit" accesskey="E"
							onclick="return csvReport();"><b><u>E</u></b>xport to CSV</button>
					</td>
				</tr>
			</table>

		</form>
	</body>
</html>

