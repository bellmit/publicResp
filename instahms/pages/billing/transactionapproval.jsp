<%@page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%
	String resultMsg = "";
	if (request.getAttribute("resultMsg") != null) {
		resultMsg = request.getAttribute("resultMsg").toString();
	}
%>
<html>
<c:set var="cpath" value="${pageContext.request.contextPath }"/>
<head>
	<title>Transaction Approval - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="/billing/transactionapproval.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<style type="text/css">
		td.forminfo { font-weight: bold; }
		tr.deleted {background-color: #F2DCDC; color: gray; }
		tr.deleted input {background-color: #F2DCDC; color: gray;}
	    td.bgNone input {WIDTH: 64px; BORDER-TOP-STYLE: none; BORDER-RIGHT-STYLE: none;
	    				BORDER-LEFT-STYLE: none; HEIGHT: 15px; BACKGROUND-COLOR: transparent; BORDER-BOTTOM-STYLE: none;}

	</style>

</head>
<script type="text/javascript">
var cpath="${cpath}";

function validateSearch(){
	if (!doValidateDateField(document.getElementById("fdate"))) {
		document.getElementById("fdate").value="";
		return false;
	}
	if (!doValidateDateField(document.getElementById("tdate"))) {
		document.getElementById("tdate").value="";
		return false;
	}
	return true;
}
</script>

<body  class="yui-skin-sam" onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Transaction Approval</h1>

	<insta:feedback-panel/>

	<form name="transactionApprovalForm" method="get">

		<input type="hidden" name="_method" value="getTransactionApprovalScreen"/>
		<input type="hidden" name="_searchMethod" value="getTransactionApprovalScreen"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>

		<insta:search form="transactionApprovalForm" optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">MR No:</div>
					<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<input type="hidden" name="mr_no@op" value="ilike" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel">Bill No:</div>
					<div class="sboFieldInput">
						<input type="text" name="bill_no" value="${ifn:cleanHtmlAttribute(param.bill_no)}">
						<input type="hidden" name="bill_no@op" value="ico" />
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Bill Date:</div>
							<div class="sfField">
							<div class="sfFieldSub">From:</div>
								<insta:datewidget name="finalized_date" id="fdate" valid="past" value="${paramValues.finalized_date[0]}"/>
							</div>
							<div class="sfField">
							<div class="sfFieldSub">To:</div>
								<c:choose>
									<c:when test="${(null != paramValues.finalized_date[1]) && ('' != paramValues.finalized_date[1]) }">
										<insta:datewidget name="finalized_date" id="tdate" value="${paramValues.finalized_date[1]}"  valid="past"	/>
									</c:when>
									<c:otherwise>
										<insta:datewidget name="finalized_date" value="today"  valid="past"	/>
									</c:otherwise>
								</c:choose>
								<input type="hidden" name="finalized_date@op" value="ge,le"/>
							</div>
						</td>
						<td>
							<div class="sfLabel">Patient Type:</div>
							<div class="sfField">
								<insta:checkgroup name="visit_type" opvalues="i,o" optexts="IP,OP" selValues="${paramValues.visit_type}"/>
								<input type="hidden" name="visit_type@op" value="in" />
							</div>
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Transaction Type:</div>
							<div class="sfField">
								<insta:checkgroup name="insurance_payable" opvalues="Y,N" optexts="Insurance, Non Insurance" selValues="${paramValues.insurance_payable}"/>
								<input type="hidden" name="insurance_payable@op" value="in" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Bill Status:</div>
							<div class="sfField">
								<insta:checkgroup name="bill_status" opvalues="F,S,C" optexts="Finalized,Settled,Closed" selValues="${paramValues.bill_status}"/>
								<input type="hidden" name="bill_staus@op" value="in" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Charge Group:</div>
							<div class="sfField">
								<insta:checkgroup name="charge_group" opvalues="REG,DOC,OPE,BED,ICU,OTC,MED,SNP,DIA" optexts="Registration,Doctor,Operation,Ward,ICU,OtherCharges,Medicine,
								Services,Tests" selValues="${paramValues.charge_group}"/>
								<input type="hidden" name="charge_group@op" value="in" />
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>

	</form>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<form name="mainform" method="post" action="TransactionApprovalAction.do">

		<input type="hidden" name="_method" value="saveTransactionApprovals"/>
		<input type="hidden" name="noOfUpdates" id="noOfUpdates" value="0" />
		<input type="hidden" name="noOfDeletes" id="noOfDeletes" value="0">
		<input type="hidden" name="noOfApprovals" id="noOfApprovals" value="0">

		<div class="resultList">
		<table class="resultList" width=" cellspacing="0" cellpadding="0" id="table1">
			<tr>
			 	<insta:sortablecolumn name="bill_no" title="Bill No"/>
			 	<insta:sortablecolumn name="patient_name" title="Patient"/>
			 	<insta:sortablecolumn name="status" title="St"/>
			 	<insta:sortablecolumn name="finalized_date" title="Date"/>
			 	<insta:sortablecolumn name="charge_head" title="Charge Head"/>
			 	<insta:sortablecolumn name="act_description" title="Description"/>
				<th style="width:6em">Rate</th>
				<th style="width:3em">Qty</th>
				<th style="width:5em">Disc</th>
				<th style="width:6em">Amt</th>
				<th style="width:3em">Upd</th>
				<th style="width:3em">X</th>
				<th style="width:3em">Appr</th>

			</tr>


	<c:forEach items="${pagedList.dtoList}" var="dtoList" varStatus="status">
			<c:set var="i" value="${status.index + 1}"/>
		<tr class="${status.index == 0 ? 'firstRow' : ''} ${status.index % 2 == 0 ? 'even' : 'odd'}"
			onclick="showToolbar(${status.index}, event, 'table1',
				{billNo: '${dtoList.bill_no}'},'');" id="toolbarRow${status.index}">
		<td >
			${dtoList.bill_no}
			<input type="hidden" name="billStatus" id="billStatus${i}" value="${dtoList.bill_status}"/>
		</td>
	<td >
		<c:if test="${dtoList.visit_type eq 'r'}">${dtoList.customer_name}</c:if>
		<c:if test="${not(dtoList.visit_type eq 'r') }"><insta:truncLabel value="${dtoList.patient_name} ${dtoList.middle_name} ${dtoList.last_name}" length="15"/></c:if>
	</td>
	<td>${dtoList.status}
		<input type="hidden" name="chargeStatus" id="chargeStatus${i}" value="${dtoList.status}">
	</td>
	<td><fmt:formatDate value="${dtoList.finalized_date}" pattern="dd-MM-yyyy"/>
		<input type="hidden" name="postedDate" id="postedDate${i}" value="${dtoList.finalized_date}">
	</td>
	<td>${dtoList.charge_head}</td>
	<td><insta:truncLabel value="${dtoList.act_description}" length="30"/></td>

		<td>
			<input type="text" name="rate" id='rate${i}' value="${dtoList.act_rate}" class="number"
				onblur="return onChangeAmount(${i});" onkeypress="return enterNumOnly(event);" readonly="readonly"/>
			<input type="hidden" id="origRate${i}" value="${dtoList.act_rate}"/>
		</td>
		<td>
			<input type="text" name="qty" id='qty${i}' value="${dtoList.act_quantity}" class="number"
				onblur="return onChangeAmount(${i});" onkeypress="return enterNumOnly(event);"
				readonly="readonly" style="width:3em"/>
			<input type="hidden" id="origQty${i}" value="${dtoList.act_quantity}"/>
		</td>
		<td>
			<input type="text" name="discount" id='discount${i}' value="${dtoList.discount}" class="number"
				onblur="return onChangeAmount(${i});" onkeypress="return enterNumOnly(event);"
				readonly="readonly"/>
			<input type="hidden" id='origDiscount${i}' value="${dtoList.discount}"/>
		</td>

		<td>
			<input type="text" name="amount" id='amount${i}' value="${dtoList.amount}" class="number"
						readonly="readonly" />
		  <input type="hidden" name="origAmount" id='origAmount${i}' value="${dtoList.amount}"/>
		</td>

		<td><input type="checkbox" name="updatedRow" id='updatedRow${i}' onclick="onEdit(this,${i})" value="${i}" style="width:3em"/>	</td>
		<td><input type="checkbox" name="deletedRow" id='deletedRow${i}' onclick="onDelete(this,${i})" value="${i}" style="width:3em"/>	</td>
		<td><input type="checkbox" name="approvedRow" id='approvedRow${i}' value="${i}" style="width:3em"/>
		<input type="hidden" name="chargeId" id="chargeId${i}" value="${dtoList.charge_id}"/>
		<input type="hidden" name="remarks" id="remarks${i}" value="${dtoList.act_remarks}"/>
		<input type="hidden" name="billNo" id="billNo${i}" value="${dtoList.bill_no}"/>
			</td>
	</tr>
		<c:set var="totalAmount" value="${totalAmount + dtoList.amount}"/>
 			</c:forEach>
			 <tr>
			 <td colspan="8" style="text-align:right">Total Amount:</td>
			 <td ><input type="text" class="number" readonly id="totalAmount" value="${totalAmount}"></td>
			 <td style="text-align:right"> Select:</td>
			 <td>
			 	<a href="javascript:void(0)" onclick="return updateChk();"
			 	style="width: 25px; border-top-style: none; border-right-style: none; border-left-style: none;
			 	 height: 15px; background-color: transparent; border-bottom-style: none">All</a>
			 </td>
			 <td>
			 <a href="javascript:void(0)" onclick="return deleteChk();"
			 	style="width: 25px; border-top-style: none; border-right-style: none; border-left-style: none;
			 	 height: 15px; background-color: transparent; border-bottom-style: none">All</a>
			 </td>
			 <td>
			 <a href="javascript:void(0)" onclick="return approveChk();"
			 	style="width: 25px; border-top-style: none; border-right-style: none; border-left-style: none;
			 	 height: 15px; background-color: transparent; border-bottom-style: none">All</a>
			 </td>
			</tr>
			<tr>
			 <td colspan="8" style="text-align:right">Original Total Amount:</td>
			 <td ><input type="text" class="number" readonly id="origTotalAmount" value="${totalAmount}"></td>
			 <td style="text-align:right"> Select:</td>
			 <td>
			 	<a href="javascript:void(0)" onclick="return updateUnChk();"
			 	style="width: 35px; border-top-style: none; border-right-style: none; border-left-style: none;
			 	 height: 15px; background-color: transparent; border-bottom-style: none">None</a>
			 </td>
			 <td>
			 	<a href="javascript:void(0)" onclick="return deleteUnChk();"
			 	style="width: 35px; border-top-style: none; border-right-style: none; border-left-style: none;
			 	 height: 15px; background-color: transparent; border-bottom-style: none">None</a>
			 </td>
			 <td>
			 	<a href="javascript:void(0)" onclick="return apprUnChk();"
			 	style="width: 35px; border-top-style: none; border-right-style: none; border-left-style: none;
			 	 height: 15px; background-color: transparent; border-bottom-style: none">None</a>
			 </td>
			</tr>
			<tr>
			 <td colspan="8" style="text-align:right">Increase:</td>
			 <td ><input type="text" class="number" readonly id="diffAmount" value="0"></td>
			 <td colspan="4"></td>
			 </tr>
			<c:url var="searchURLWithoutPage" value="/pages/BillDischarge/TransactionApprovalAction.do">
				<%-- add all the request parameters as parameters to the search URL --%>
				<c:forEach var="p" items="${param}">
					<c:if test="${p.key != 'pageNum'}">
						<c:param name="${p.key}" value="${p.value}"/>
					</c:if>
				</c:forEach>
			</c:url>
		</table>

			<c:if test="${param._method == 'getTransactionApprovalScreen'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

	</div>
	<table style="margin-top: 10px">
			<tr>
				<td><b>Percentage:</b>&nbsp;
					<input type="text" name="perCen" id="perCen" class="number" onkeypress="return enterNumOnly(event);" style="width:4em"/>%</td>

                <td>&nbsp;&nbsp;<b>Rate Variation from Base:</b>&nbsp;
					<select name="rateVariation" id="rateVariation" style="width:8em" class="dropdown">
						<option value="I">Increase</option>
						<option value="D">Decrease</option>

					</select></td>

				<td>&nbsp;&nbsp;<b>Round off to nearest:</b>&nbsp;
					<select name="rRs"id="rRs"  style="width:5em" class="dropdown">
						<option value="N">None</option>
						<option value="10">10</option>
						<option value="25">25</option>
						<option value="50">50</option>
						<option value="100">100</option>
					</select></td>
				<td>&nbsp;&nbsp;<input type="button" name="" value="Apply" onclick="reCalRate()"/></td>
			</tr>
		</table>
	<table class="screenActions" style="display:${hasResults?'block':'none'}">
		<tr>
			<td>
				<input type="submit" class="button"  value="Save" onclick="return validateSave();"/>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td>
				<a href="#" onclick="return resetAll();">Reset</a>
			</td>
		</tr>
	</table>



</form>
</body>
</html>
