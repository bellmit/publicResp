<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>

<head>
	<title>Write-offs - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

	<style type="text/css">
		.status_A.type_P { background-color: #EAD6BB }
		.status_C { background-color: #C5D9A3 }
		.status_X { color: grey }
		table.legend { border-collapse: collapse; margin-left: 6px; }
		table.legend td { border: 1px solid grey; padding: 2px 5px;}
	</style>

<script type="text/javascript">
function validateSearch() {
	if(document.forms[0].fdate.value == "") {
		alert("From date required");
		return false;
	}
	if(document.forms[0].tdate.value == "") {
		alert("To date required");
		return false;
	}
	if(!doValidateDateField(document.forms[0].fdate)){
		return false;
	}
	if(!doValidateDateField(document.forms[0].tdate)){
		return false;
	}
	document.forms[0].method.value="getWriteOff";
	document.forms[0].submit();
	return true;
}

function enablePatientType() {
	var patientAll = document.forms[0].patientAll.checked;
	var disabled = patientAll;

	document.forms[0].patientIp.disabled = disabled;
	document.forms[0].patientOp.disabled = disabled;
	return true;
}

function clearSearch() {
	var theForm = document.forms[0];
	theForm.fdate.value = "";
	theForm.tdate.value = "";
	theForm.patientAll.checked = true;
	enablePatientType();
}

function init() {
	var checkAll = false;
	checkAll = checkAll || document.forms[0].patientIp.checked;
	checkAll = checkAll || document.forms[0].patientOp.checked;
	if(!checkAll)
		document.forms[0].patientAll.checked = true;
	enablePatientType();
}

function getWriteOffReport(){

	if(!doValidateDateField(document.forms[0].fdate)){
		return false;
	}
	if(!doValidateDateField(document.forms[0].tdate)){
		return false;
	}

	var patientType = "";
	var temp ="";
	if (document.forms[0].patientAll.checked){
		temp = "";
		patientType = "";
	}
	if (document.forms[0].patientIp.checked) {		
		temp = "i";
		patientType = patientType+"'"+temp+"'";
	}
	if (document.forms[0].patientOp.checked){
		if (temp !="")
			patientType = patientType+",";
		temp ="o";
		patientType = patientType+"'"+temp+"'";
	}
	var fromDate = document.forms[0].fdate.value;
	var toDate = document.forms[0].tdate.value;

	window.open("patientdues.do?method=getWriteoffReport&patientType="+patientType+"&fromdate="+fromDate+"&todate="+toDate);
}
	</script>

</head>



<body onload="init()">
	<div class="pageHeader">Write Offs</div>
	<html:form action="/pages/Enquiry/patientdues.do" method="GET">
	<input type="hidden" name="method" value="getWriteOff"/>

	<div class="stwMain">
			<div class="stwHeader ${ifn:cleanHtmlAttribute(param.filterClosed) ? 'stwClosed' : ''}" id="filter" onclick="stwToggle(this);">
				<label>Filter</label>
			</div>
			<div id="filter_content" class="stwContent ${ifn:cleanHtmlAttribute(param.filterClosed) ? 'stwHidden' : ''}">

	<table align="center" class="search" width="100%">
		<tr>
			<th>Bill Date:</th>
			<th>Patient Type:</th>
		</tr>
		<tr>
			<td>
				From Date <insta:datewidget name="fdate"  valid="past" value="${param.fdate}" /> </br>
				To Date&nbsp;&nbsp;&nbsp;&nbsp;
				<c:choose>
						<c:when test="${(null != param.tdate) && ('' != param.tdate) }">
							<insta:datewidget name="tdate" value="${param.tdate}"  valid="past"	/>
						</c:when>
						<c:otherwise>
							<insta:datewidget name="tdate" value="today"  valid="past"	/>
						</c:otherwise>
				</c:choose>
			</td>
			<td>
				<html:checkbox property="patientAll" onclick="enablePatientType()">(All)
					</html:checkbox><br/>
				<html:checkbox property="patientIp">IP</html:checkbox><br/>
				<html:checkbox property="patientOp">OP</html:checkbox><br/>
			</td>

		</tr>
		 <tr>
			<td  align="center"></td>
       		<td  align="right">
				<input type="submit"  value="Search"  onclick="return validateSearch()"/>
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

	<c:set var="summary" value="${WriteOffList.countInfo}"/>

	<fieldset class="fieldSetBorder">
		<table style="width: 100%">
			<tr>
				<td>Bill Total: <b>${summary.sum_amount}</b></td>
				<td>Claim Total: <b>${summary.sum_claim}</b></td>
				<td>Paid Total: <b>${summary.sum_receipts}</b></td>
				<td>Write Off Total: <b>${summary.sum_amount - summary.sum_receipts - summary.sum_claim}</b></td>
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
			<th>Write Off</th>
		</tr>

		<c:forEach items="${WriteOffList.dtoList}" var="obj" varStatus="status">
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

	<table>
		<tr>
			<td>
				<c:choose>
					<c:when test="${WriteOffList.totalRecords > 100 }">
						<b>Note:</b> The search resulted is more than 100 items, only the first 100 matches are shown.
					</c:when>
					<c:otherwise>
						<c:choose>
							<c:when test="${WriteOffList.totalRecords <= 0}">
								<b>Note:</b> Search returned no results.
							</c:when>
						</c:choose>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
	</table>
	<table align="center">
		<tr>
			<td>&nbsp;</td>
		</tr>
		<tr>
				<td><input type="submit" name="print" value="Print" onclick="return getWriteOffReport()"/></td>
		</tr>
	</table>

</body>
</html>

