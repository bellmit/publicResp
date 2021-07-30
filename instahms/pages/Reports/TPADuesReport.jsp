<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
<title>TPA Dues Report - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="reports/tpawise_patientdues_report.js"/>

	<style type="text/css">
		.status_A.type_P { background-color: #EAD6BB }
		.status_C { background-color: #C5D9A3 }
		.status_X { color: grey }
		table.legend { border-collapse: collapse; margin-left: 6px; }
		table.legend td { border: 1px solid grey; padding: 2px 5px;}
	</style>

</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="patientDuesList" value="${pagedList.dtoList}"/>
<body >
	<div class="pageHeader">TPA/Sponsor Dues Report</div>
	<html:form method="POST" action="/pages/Enquiry/TPADuesReport.do">
	<input type="hidden" name="method" value="getTpaDuesReportScreen"/>
	<html:hidden property="sortOrder" value="${param.sortOrder}"/>
	<html:hidden property="sortReverse" value="${param.sortReverse}"/>



	<div class="stwMain">
			<div class="stwHeader" id="filter" onclick="stwToggle(this);">
				<label>Filter</label>
			</div>
			<div id="filter_content" class="stwContent">

	<table align="center" class="search" width="100%">
		<tr>
			<th>TPA/Sponsor Name:</th>
		</tr>
		<tr>
			<td>
				<html:select property="tpaNames" multiple="true" size="6">
					<c:forEach var="tpaName" items="${tpaList}">
						<html:option value="${tpaName.TPA_ID}">${tpaName.TPA_NAME}</html:option>
					</c:forEach>
				</html:select>
			</td>
		</tr>
		 <tr>
       		<td  align="right">
       			<input type="submit"  value="Search" />
       			<input type="reset" value="Reset">
       			<input type="button" value="Clear" onclick="clearSearch()">
			</td>
		</tr>

	</table></div>
  </div>
  	<script>
		stwOpen(document.getElementById('filter'), ${filterClosed});
	</script>
<table>
<tr style="height: 1em"><td></td></tr>
</table>

	<c:choose>
		<c:when test ="${not empty patientDuesList}" >
			<table class="dashboard" cellspacing="0" cellpadding="0" align="center" width="100%">
				<tr>
					<insta:sortablecolumn name="tpa_name" title="TPA/Sponsor Name"/>
					<insta:sortablecolumn name="mr_no" title="MR No"/>
					<th>Patient Id</th>
					<th>Patient Name</th>
					<insta:sortablecolumn name="case_added_date" title="Case Open Date"/>
					<th>Claim Amount</th>
					<th>Amount Received</th>
					<th>Amount Due</th>
				</tr>
				<c:forEach var="patient" items="${patientDuesList}" varStatus="st">
					<tr >
						<td>${patient.tpaName}</td>
						<td>${patient.mrNo}</td>
						<td>${patient.patientId}</td>
						<td>${patient.salutation} ${patient.patientName} ${patient.middleName} ${patient.lastName}</td>
						<td><fmt:formatDate value="${patient.caseOpenDate}" pattern="dd-MM-yyyy"/></td>
						<fmt:formatNumber maxFractionDigits="2" pattern="0.00" var="claimamount" value="${patient.claimAmount}" />
						<fmt:formatNumber maxFractionDigits="2" pattern="0.00" var="receivedamount" value="${patient.receivedAmount}" />
						<fmt:formatNumber maxFractionDigits="2" pattern="0.00" var="dueamount" value="${patient.claimAmount - patient.receivedAmount}" />
						<td class="number">${claimamount}</td>
						<td class="number">${receivedamount}</td>
						<td class="number">${dueamount}</td>
					</tr>
				</c:forEach>

			</table>
			<insta:paginate numPages="${pagedList.numPages}" curPage="${pagedList.pageNumber}" baseUrl="TPADuesReport.do" totalRecords="${pagedList.totalRecords}"/>
		</c:when>

		<c:otherwise>
			<c:if test="${param.method == 'getTpaDuesReportScreen'}">
				<div>
					<p>No Result found for the given search criteria </p>
				</div>
			</c:if>
		</c:otherwise>
	</c:choose>


</html:form>


</body>
</html>
