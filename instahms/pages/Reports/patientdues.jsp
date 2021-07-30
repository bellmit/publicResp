<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>Patient Dues - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<jsp:include page="/pages/yuiScripts.jsp"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="reports/patientdues.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

	<style type="text/css">
		.status_A.type_P { background-color: #EAD6BB }
		.status_C { background-color: #C5D9A3 }
		.status_X { color: grey }
		table.legend { border-collapse: collapse; margin-left: 6px; }
		table.legend td { border: 1px solid grey; padding: 2px 5px;}
	</style>

</head>

<body onload="init()">
	<div class="pageHeader">Patient Dues</div>
	<html:form method="GET" action="/pages/Enquiry/patientdues.do">
	<input type="hidden" name="method" value="getPatientDues"/>

	<div class="stwMain">
		<div class="stwHeader ${ifn:cleanHtmlAttribute(param.filterClosed) ? 'stwClosed' : ''}" id="filter" onclick="stwToggle(this);">
			<label>Filter</label>
		</div>
		<div id="filter_content" class="stwContent ${ifn:cleanHtmlAttribute(param.filterClosed) ? 'stwHidden' : ''}">

		<table align="center" class="search" width="100%">
			<tr>
				<th>Patient Type:</th>
				<th>Status:</th>
				<th>MR No</th>
				<th>Admission Date</th>
			</tr>
			<tr>
				<td>
					<html:checkbox property="patientAll" onclick="enablePatientType()">(All)</html:checkbox><br/>
					<html:checkbox property="patientIp">IP</html:checkbox><br/>
					<html:checkbox property="patientOp">OP</html:checkbox><br/>
				</td>
				<td>
					<html:checkbox property="statusAll" onclick="enableStatus();">(All)</html:checkbox><br/>
					<html:checkbox property="statusInActive">InActive</html:checkbox><br/>
					<html:checkbox property="statusActive">Active</html:checkbox><br/>
				</td>
				<td>
					<html:text property="mrNo" onkeypress="onKeyPressMrno(event)" onblur="onChangeMrno()"></html:text><br/>
				</td>
				<td>
				<c:choose> <c:when test="${not empty fdate}">
				 <insta:datewidget name="fdate" id="fdate"  value="${fdate}" btnPos="left"/> </c:when>
				 <c:otherwise><insta:datewidget name="fdate" id="fdate"  btnPos="left"/></c:otherwise>
				 </c:choose>
				<br>
				<c:choose><c:when test="${not empty tdate}">
					<insta:datewidget name="tdate" id="tdate" value="${tdate}" btnPos="left"/></c:when>
						<c:otherwise><insta:datewidget name="tdate" id="tdate"  btnPos="left"/></c:otherwise>
				</c:choose>
				</td>
		</tr>

		 <tr>
     		<td  align="right" colspan="4">
					<input type="submit"  value="Search" onclick="return resetPageValues()"/>
					<input type="reset" value="Reset">
       		<input type="button" value="Clear" onclick="clearSearch()">
				</td>
			</tr>
	</table></div>
  </div>
<table>
<tr style="height: 1em"><td></td></tr>
</table>
</html:form>



<form action="/pages/Enquiry/patientdues.do" method="GET" >
	<input type="hidden" name="method" value="getPatientDues"/>

	<c:set var="summary" value="${DuePatientList.countInfo}"/>

	<fieldset class="fieldSetBorder">
		<table style="width: 100%">
			<tr>
				<td>Bill Total: <b>${summary.sum_amount}</b></td>
				<td>Claim Total: <b>${summary.sum_claim}</b></td>
				<td>Paid Total: <b>${summary.sum_receipts}</b></td>
				<td>Due Total: <b>${summary.sum_amount - summary.sum_receipts - summary.sum_claim}</b></td>
			</tr>
		</table>
	</fieldset>

	<table class="detailFormTable" width="100%" cellspacing="0" cellpadding="0" id="table1">
		<tr bgcolor="#8FBC8F" style="width: 100%">
		 	<th>MR No</th>
		 	<th>Visit Id</th>
		 	<th>Patient Name</th>
		 	<th>Bill No</th>
		 	<th>Finalized Date</th>
			<th>Bill Amount</th>
			<th>Claim Amount</th>
			<th>Paid Amount</th>
			<th>Due Amount</th>
		</tr>

		<c:forEach items="${DuePatientList.dtoList}" var="obj" varStatus="status">
			<tr>
				<td>${obj.mr_no}</td>
				<td>${obj.visit_id}</td>
				<td>${obj.patient_full_name}</td>
				<td>
					<a href="${cpath}/billing/BillAction.do?method=getCreditBillingCollectScreen&billNo=${obj.bill_no}" >${obj.bill_no}</a>
				</td>
				<td><fmt:formatDate value="${obj.finalized_date}" pattern="dd-MM-yyyy"/></td>
				<td align="right">${obj.total_amount}</td>
				<td align="right">${obj.total_claim}</td>
				<td align="right">${obj.total_receipts}</td>
				<td align="right">${obj.total_amount - obj.total_claim - obj.total_receipts}</td>
			</tr>
		</c:forEach>
	</table>


	<insta:paginate curPage="${DuePatientList.pageNumber}" numPages="${DuePatientList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<table align="center">
		<tr>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>
				<input type="button" name="print" value="Print" onclick="return getReport();"/>
			</td>
		</tr>
	</table>

</body>
</html>

