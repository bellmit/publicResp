<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>Outsource Payments - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="js" file="hmsvalidation.js"/>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="/payments/outhousepayment.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<style>
		.scrollcontainer .yui-ac-content{
			max-height:11em; overflow:auto;overflow-x:hidden;
			_height:11em;
		}
	</style>
</head>
<c:set var="method_name" value="getOutHousePaymentScreen"/>
<c:set var="charge" value="${ohChargeList.dtoList}"/>
<c:set var="postedCharge" value="${ohPostedList.dtoList}"/>
<c:set var="counter" value="${requestScope.counterList}"/>
<c:set var="hasResult" value="${(not empty charge) || (not empty postedCharge)}"/>
<body onload="init();">
	<div class="pageHeader">Outsource Payments Screen</div>
	<form method="GET" action="${cpath}/pages/payments/OuthouseTestsPayment.do" name="outhouseSearchForm">
	<input type="hidden" name="_method" value="outHouseSearch" id="method"/>
	<input type="hidden" name="_searchMethod" value="outHouseSearch" id="searchMethod"/>
		<div id="dialog1" style="visibility:hidden">
			<div class="hd">Edit</div>
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Edit Out house payment</legend>
					<input type="hidden" id="editRowId" value=""/>
					<table class="formtable">
						<tr>
							<td class="formlabel">Outhouse Payment :</td>
							<td><input type="text" name="_dialog_ohFees" size="5" class="number"
									id="_dialog_ohFees" onkeypress="return enterNumOnly(event);" onblur="updateStatus();"/>
							</td>
						</tr>
					</table>
					<table style="margin-top: 10px;">
						<tr>
							<td><input type="button" id="editOk" name="editOk" value="Ok"/></td>
							<td><input type="button" id="editDialogPrevious" name="editDialogPrevious" value="<<Previous"/></td>
							<td><input type="button" id="editDialogNext" name="editDialogNext" value="Next>>"/></td>
							<td><input type="button" id="editDialogCancel" name="editDialogCancel" value="Close" /></td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
		<insta:search form="outhouseSearchForm" optionsId="optionalFilter" closed="${hasResult}"
			validateFunction="getOuthouseCharges()">
			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">Outsource:</div>
					<div class="sboFieldInput">
						 <div id="outhouseDiv">
								<input type="text" name="_outhouseName" id="outhouseName" value="${ifn:cleanHtmlAttribute(param._outhouseName)}"/>
						    <div id="outhouseList" style="width: 30em"></div>
						 </div>
					</div>
				</div>
				<input type="hidden" name="outhouse_id" id="outhouseId" value="${ifn:cleanHtmlAttribute(param.outhouse_id)}"/>
			</div>
			<div id="optionalFilter" style="clear:both; display: ${hasResult ? 'none': 'block'}">
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Patient Name/MR No:</div>
							<div class="sfField">
								<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" style="width:138px"/>
								<input type="hidden" name="mr_no@op" value="like"/>
								<div id="mrnoContainer"/>
							</div>
						</td>
						<td class="last">
								<div class="sfLabel">Date:</div>
								<div class="sfField">
									<div class="sfFieldSub">From:</div>
										<insta:datewidget name="finalized_date" id="finalized_date0"
										value="${paramValues.finalized_date[0]}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub">To:</div>
										<insta:datewidget name="finalized_date" id="finalized_date1"
											value="${paramValues.finalized_date[1]}"/>
											<input type="hidden" name="finalized_date@op" value="ge,le"/>
											<input type="hidden" name="finalized_date@cast" value="y"/>
								</div>
							</td>
							<td class="last">&nbsp;</td>
							<td class="last">&nbsp;</td>
							<td class="last">&nbsp;</td>
						</tr>
				</table>
			</div>
		</insta:search>
	</form>
	<form name="outhousetestForm" method="POST" action="${cpath}/pages/payments/OuthouseTestsPayment.do">
	<input type="hidden" name="_method" value="createOhPaymentDetails" id="method"/>
	<input type="hidden" name="_noOfCharges" id="noOfCharges" value="0"/>
	<input type="hidden" name="_allCharges" id="allCharges" value="0"/>
	<input type="hidden" name="_deleteRows" id="deleteRows" value="0" />
	<input type="hidden" name="outhouse_id" id="outhouse_id" value="${ifn:cleanHtmlAttribute(param.outhouse_id)}"/>
	<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>

<insta:paginate curPage="${ohChargeList.pageNumber}" numPages="${ohChargeList.numPages}" pageNumParam="_chargePageNum" totalRecords="${ohChargeList.totalRecords}"/>
	<div id="payment_content" class="resultList">
	<table class="resultList" width="100%" cellspacing="0" cellpadding="0" id="paymentTable" align="center" onmouseover="hideToolBar()">
		<tr onmouseover="hideToolBar()">
			<th>Select</th>
			<th>MR No</th>
			<th>Bill No</th>
			<th>Date</th>
			<th>Charge Head</th>
			<th>Description</th>
			<th class="number">Billed Amt</th>
			<th class="number">Outsource Payment</th>
			<th></th>
		</tr>
		<c:forEach items="${charge}" var="oc" varStatus="st">
			<c:set var="i" value="${st.index+1}"/>
			<tr style="${st.index == 0 ? 'firstRow':''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
				onclick="showToolbar(${st.index}, event, 'paymentTable', {billNo: '${oc.bill_no}'})"
				onmouseover="hideToolBar('${st.index}')"
				id="toolbarRow${st.index}">
				<td>
				  	 <input type="checkbox" name="statusCheck" value="${i}" onclick="editOhPayment(this,${i})"/>
				</td>
				<td>
					${oc.mr_no}<input type="hidden" name="_mrNo"  value="${oc.mr_no}"/>
				</td>
				<td>
					<label name="bill" title="${oc.bill_type}/${oc.status}/${oc.visit_type}">	${oc.bill_no}</label>
					<input type="hidden" name="_billNo" value="${oc.bill_no}"/>
					<input type="hidden" name="_chargeId" value="${oc.charge_id}"/>
				</td>
				<td>
						<fmt:formatDate value="${oc.finalized_date}" pattern="dd-MM-yyyy"/>
						<input type="hidden" name="_postedDate" value="${oc.finalized_date}"/>
				</td>
				<td>
					${oc.chargehead_name}<input type="hidden" name="_chargeHeadName" value="${oc.chargehead_name}"/>
				</td>
				<td>
					${oc.act_description}<input type="hidden" name="_actDescription" value="${oc.act_description}"/>
				</td>
				<td class="number">
					${oc.amount}<input type="hidden" name="_amount" value="${oc.amount}"/>
				</td>
				<td class="number">
					<label id="doctorFee">${oc.out_house_amount}</label>
					<input type="hidden" name="_ohPayment" value="${oc.out_house_amount}"	id="ohAmount${i}" />
					<input type="hidden" id="orginalOhAmount${i}" value="${oc.out_house_amount}"/>
					<input type="hidden" name="_billAmount" id="billAmount${i}" value="${oc.amount}"/>
					<input type="hidden" name="_centerId" value="${oc.center_id}"/>
				</td>
				<td>
					<a name="_editAnchor" id="editAnchor${i}" href="javascript:Edit" onclick="return showEditChargeDialog(this);"
						title="Edit Item Details">
						<img src="${cpath}/icons/Edit.png" class="button noToolbar" />
					</a>
				</td>
			</tr>
		</c:forEach>
	</table>
	<c:if test="${not empty charge}">
	<table width="100%" style="padding-top: 10">
		<tr>
			<td>
				<div style="float:left">
					<input type="radio" name="_selectItems" id="singleItem" value="item" checked="true"
					onclick="onCheckRadio(this.value)">Select Single Item
				</div>
				<div style="float:left">
					<input type="radio" name="_selectItems" id="pageItems" value="pageItems"
					onclick="onCheckRadio(this.value)">Select Page Items
				</div>
				<div style="float:left">
					<input type="radio" name="_selectItems" id="allItems" value="all" onclick="onCheckRadio(this.value);">Select All Items
				</div>
				<div style="float: right">
					Items Total: <label id="allTotAmt" style="font-weight: bold">0</label>
				</div>
			</td>
		</tr>
	</table>
	<div class="screenActions" style="margin-bottom: 10px">
		<input type="button" name="save" value="Save" onclick="return saveCharges();" />
	</div>
</div>
</c:if>

	<c:if test="${param._method != 'getOutHousePaymentScreen'}">
			<insta:noresults hasResults="${not empty charge}"/>
	</c:if>
						 <!--	POSTING PAYMENTS	-->

	<dl class="accordion">
		<dt style="margin-top:10px"><span class="clrboth">View Posted Payments</span></dt>
		<dd class="${not empty postedCharge? 'open' : ''}" style="width:952px">
		<div class="bd">
			<insta:paginate curPage="${ohPostedList.pageNumber}" numPages="${ohPostedList.numPages}" pageNumParam="_paymentPageNum" totalRecords="${ohPostedList.totalRecords}"/>
			<div id="viewDiv" class="resultList">
				<table class="resultList" cellpadding="0" cellspacing="0" width="100%" id="postedPaymentTable" onmoveover="hideToolBar()">
					<tr onmouseover="hideToolBar('')">
						<th><input type="checkbox" name="deleteAll" onclick="deleteAllCharges()"/>Delete</th>
						<th>MR No</th>
						<th>Bill No</th>
						<th>Date</th>
						<th>Charge Head</th>
						<th>Description</th>
						<th class="number">Billed Amt</th>
						<th class="number">Outsource Payment</th>
					</tr>
					<c:forEach items="${postedCharge}" var="op" varStatus="st">
					<c:set var="j" value="${st.index+1}"/>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclcik="showToolbar(${st.index}, event, 'postedPaymentTable', {billNo: '${op.bill_no}'})" onmouseover="hideToolBar('${st.index}')"
						id="toollbarRow${st.index}">
						<td><input type="checkbox" name="deleteCharge" id="deleteCharge${j}" value="${j}" onclick="deleteDoctorCharge(this,${j});"/></td>
						<td>
							${op.mr_no}<input type="hidden" name="_delmrNo"  value="${op.mr_no}" />
						</td>
						<td>
							<label name="bill" title="${op.bill_type}/${op.status}/${op.visit_type}">${op.bill_no}</label>
							<input type="hidden" name="_delbillNo" value="${op.bill_no}" />
							<input type="hidden" name="_delchargeId" value="${op.charge_id}"/>
							<input type="hidden" name="_delPaymentId" value="${op.oh_payment_id}"/>
						</td>
						<td>
							<fmt:formatDate value="${op.finalized_date}" pattern="dd-MM-yyyy"/>
							<input type="hidden" name="_delpostedDate" value="${op.finalized_date}" />
						</td>
						<td>
							${op.chargehead_name}<input type="hidden" name="_delchargeHeadName" value="${op.chargehead_name}" />
						</td>
						<td>
							${op.act_description}
							<input type="hidden" name="_delactDescription" value="${op.act_description}"/>
						</td>
						<td class="number">
							${op.amount}<input type="hidden" name="_delamount" value="${op.amount}"/>
						</td>
						<td class="number">${op.out_house_amount}
							<input type="hidden" name="_deldoctorFees${j}" value="${op.out_house_amount}"/>
						</td>
					</tr>
					</c:forEach>
				</table>
				<c:if test="${param._method !='getOutHousePaymentScreen'}">
						<insta:noresults hasResults="${not empty postedCharge}"/>
				</c:if>
				<c:if test="${not empty postedCharge}">
				<table class="screenActions">
					<tr>
						<td><input type="button" name="delete" value="Delete" onclick="return deleteCharges()"/></td>
					</tr>
				</table>
				</c:if>
			</div>
		</div>
		</dd>
	</dl>
</form>

<script>
	var outhouseList = ${outhouselist};
</script>
</body>
</html>
