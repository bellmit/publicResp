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

<c:set var = "contextPath" value="${pageContext.request.contextPath}"/>

<head>
	<title>Tests Count Report - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>

<script type="text/javascript">
			var fromDate, toDate,category ;

		function onInit() {
			fromDate = document.inputform.fromDate;
			toDate = document.inputform.toDate;
			setDateRangeYesterday(fromDate, toDate);
			 document.forms[0].all.checked = true;
			selectDepartments();
		}
		function validateCategory(){
			var form = document.forms[0];
			if(!validateDepartmentNames()){
				return false;
			}

			if (validateFromToDate(document.inputform.fromDate,document.inputform.toDate)){
				return true;
			}else{
				return false;
			}
		}


function selectDepartments(){
	var disable = document.forms[0].all.checked;
	var deptLen = document.forms[0].deptIdArray.length;
	for (i=deptLen-1;i>=0;i--){
		document.forms[0].deptIdArray[i].selected = disable;
	}
}

function validateDepartmentNames(){
	var deptSelected = false;
	var deptLen = document.forms[0].deptIdArray.length;
	var chkAll = document.forms[0].all.checked;
	var depIdSelected = false;
	if(!chkAll){
		for (i=0;i<deptLen;i++){
			if (document.forms[0].deptIdArray[i].selected) {
				depIdSelected = true;
			}
		}

		if (!depIdSelected){
			alert("Select atleast one department ");
			return false;
		}
	}
	return true;
}

function deselectAll(){
		document.forms[0].all.checked = false;
}


</script>
</head>

<html>
	<body onload="onInit()">
		<div class="pageHeader">Test Count Trend Report</div>
		<form name="inputform"  target="_blank" method="GET">
		<input type="hidden" name="method" value="getReport">

		<div class="tipText">
				This report shows a trend of the number of tests conducted
				within given dates
		</div>

	<table align="center">
			<tr>
				<td colspan="3" align="left">
					<jsp:include page="/pages/Common/DateRangeSelector.jsp">
						<jsp:param name="skipWeek" value="Y"/>
					</jsp:include>
				</td>
			</tr>
		</table>
		<div class="stwMain">
			<table width="100%" align="center" class="search">
				<tr>
					<th>Department Name</th>
					<th>Test Category</th>
					<th>Test Status</th>
					<th>Patient Type</th>
				</tr>
				<tr>
					<td>
						<table>
							<tr>
								<td valign="top">
									<input type="checkbox" name="all" value="all" onclick="selectDepartments();">All
								</td>
								<td>
									<insta:selectdb  name="deptIdArray"  size = "5" onclick="deselectAll()"
									multiple= "true" value="" table="diagnostics_departments" valuecol="ddept_id"
									displaycol="ddept_name" filtered="false"/>
								</td>
							</tr>
						</table>
					</td>
					<td>
						<table>
							<tr>
								<td>
									<insta:checkgroup name="categoryArray" selValues="${paramValues.categoryArray}"
									opvalues="DEP_LAB,DEP_RAD" optexts="Laboratory,Radiology" />
								</td>
							</tr>
						</table>
					</td>
					<td>
						<table>
							<tr>
								<td>
									<insta:checkgroup name="testStatusArray" selValues="${paramValues.testStatusArray}"
									opvalues="Y,N,P,Cancel" optexts="Conducted,Not Conducted,Paritally Conducted,Cancel" />
								</td>
							</tr>
						</table>
					</td>
					<td>
						<table>
							<tr>
								<td>
									<insta:checkgroup name="patientTypeArray" selValues="${paramValues.patientTypeArray}"
									opvalues="i,o" optexts="IP,OP" />
									</td>
							</tr>
						</table>
					</td>
				</tr>
				</table>
		</div>
		<table align="center" style="margin-top: 1em;">
			<tr>
				<td>
					<button type="submit" accesskey="G"
						onclick="return validateCategory()"><b><u>G</u></b>enerate Report</button>
				</td>
			</tr>

	</table>
		</form>
	</body>
</html>
