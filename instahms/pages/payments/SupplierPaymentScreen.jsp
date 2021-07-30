<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title>Supplier Payments - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="js" file="hmsvalidation.js"/>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="/payments/supplier.js"/>
	<script type="text/javascript">
			var dateType  = '${dateType}';
			var isCashpurchaseResult = '${ifn:cleanJavaScript(param._cashPurchase)}';
			var centerId = <%=session.getAttribute("centerId")%>;
			var maxcenters = <%=com.insta.hms.master.GenericPreferences.GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>;
	</script>
</head>
<c:set var="method_name" value="getSupplierPaymentScreen"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="supplierCharge" value="${supplierChargeList.dtoList}"/>
<c:set var="supplierPayment" value="${supplierPaymentList.dtoList}"/>
<c:set var="grnlink" value="${cpath}/pages/pharmacy"/>
<c:set var="hasResult" value="${(not empty supplierCharge) || (not empty supplierPayment)}"/>
<body onload="init()">
<c:set var="invoicedate">
<insta:ltext key="js.stores.procurement.invoicedate"/>
</c:set>
<div class="pageHeader">Supplier Payments </div>
<div id="dialog1" style="visibility: hidden">
	<div class="hd">Add/Edit</div>
	<div class="bd">
		<input type="hidden" id="editRowId" value=""/>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Add/Edit Description</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Description</td>
					<td ><input type="text" name="_dialog_description" id="_dialog_description" size="15" maxlength="50"onblur="updateStatus();"/></td>
				</tr>
			</table>
			<table style="margin-top: 10px">
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
<form method="GET" action="${cpath}/pages/payments/SupplierPayments.do" name="supplierPaymentSearchForm">
		<input type="hidden" name="_method" value="makeDirectPayment" />
		<input type="hidden" name="_searchMethod" value="searchSupplierCharges" />
		<insta:search form="supplierPaymentSearchForm" optionsId="optionalFilter" closed="${hasResult}"
			validateFunction="getSupplierCharges()">
			<div class="searchBasicOpts">
				<div class="sboField">
					<div clas="sboFieldLabel">Supplier:</div>
					<div class="sboFieldInput">
						<div id="supplierDiv">
							<input type="text" name="_supplierName" id="suppliers" value="${ifn:cleanHtmlAttribute(param._supplierName)}"/>
								<div id="supplierContainer" style="width: 30em"/>
						</div>
					</div>
					</div>
				</div>
				<input type="hidden" name="supplier_id" id="supplierCode" value="${ifn:cleanHtmlAttribute(param.supplier_id)}"/>
				<div class="sboField" style="white-space: nowrap">
					<div class="sboFieldInput" style="margin-top:10px">
						<input type="checkbox" name="_directPayment" id="directPayment" onclick="directPayments()">
						Direct Payments
						</div>
				</div>
				<div class="sboField" style="white-space: nowrap">
					<div class="sboFieldInput" style="margin-top:10px">
						<input type="checkbox" name="_cashPurchase" id="cashPurchase" onchange="setCashPurchase();"
						${ifn:cleanHtmlAttribute(param._cashPurchase)=='Y' ? 'checked' : '' } value="${ifn:cleanHtmlAttribute(param._cashPurchase)}">
						Cash Purchases Only
						</div>
				</div>
			</div>
			<div id="optionalFilter" style="clear:both; display: ${hasResult ? 'none' : 'block'}">
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Date Type:</div>
							<div class="sfField" style="white-space: nowrap">
								<input type="radio" name="_invoice_type" id="invoice_date"
								${paramValues._invoice_type[0] == 'invoice' ? 'checked' : ''}
								value="invoice">Invoice Date
							</div>
							<div class="sfField" style="white-space: nowrap">
								<input type="radio" name="_invoice_type" id="due_date"
								${paramValues._invoice_type[0] == 'due' ? 'checked':''}
								value="due">Due Date
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Date:</div>
							<div class="sfField">
								<div class="sfFieldSub">From: </div>
								<insta:datewidget name="_invoice_date" id="invoice_date0"
									value="${paramValues._invoice_date[0]}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub">To: </div>
									<insta:datewidget name="_invoice_date" id="invoice_date1"
									value="${paramValues._invoice_date[1]}"/>
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
<form name="supplierPaymentForm" action="${cpath}/pages/payments/SupplierPayments.do" method="POST" >
	<input type="hidden" name="_method" value="createPayments"/>
	<input type="hidden" name="_addCharge" id="addCharge" value="0"/>
	<input type="hidden" name="_allCharges" id="allCharges" />
	<input type="hidden" name="_deleteCharge" id="deleteCharge" value="0"/>
	<input type="hidden" name="_supplierName" id="supplierName"/>
	<input type="hidden" name="supplier_id" id="supplier_id" value="${ifn:cleanHtmlAttribute(param.supplier_id)}"/>
	<div id="directDiv" style="display:none">
		<fieldset class="fieldSetBorder">
			<table id="directTable" >
				<tr>
					<th>Description</th>
					<th>amount </th>
					<th>Date</th>
				</tr>
				<tr>
					<td>
						<input type="text" name="description" size="40" value=""/>
					</td>
					<td>
						<input type="text" name="amount"  size="15" value="" onkeypress="return enterNumOnly(event);"
						id="amount_0"	onblur="if (this.value != '') {roundEnteredNumber(this.value,2,0); }"/>
					</td>
					<td>
						<insta:datewidget name="_payDate" valid="past" value="today" btnPos="left" id="payDate"/>
					</td>
					<td>
						<input type="button" class="plus" name="add" value="+" onclick="addNewRow()" />
						<input type="button" class="plus" name="del" value="-" onclick="deleteRows()" />
					</td>
				</tr>
			</table>
			<table class="screenActions">
				<tr>
					<td>
						<input type="button" name="save" value="Save" onclick="return saveDirectPayments()"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
<insta:paginate curPage="${supplierChargeList.pageNumber}" numPages="${supplierChargeList.numPages}" pageNumParam="_chargePageNum" totalRecords="${supplierChargeList.totalRecords}"/>
<div id="paymentContentDiv" class="resultList">
	<table class="resultList" width="100%" cellspacing="0" cellpadding="0" id="resultTable" align="center"
			onmouseover="hideToolBar()">
			<tr onmouseover="hideToolBar()">
				<th style="width: 5em;">Select</th>
					<th>PO No</th>
					<th>Invoice No</th>
					<insta:sortablecolumn name="invoice_date" title="${invoicedate}" tooltip="${invoicedate}"/>
					<th>Due Date</th>
					<th>Description</th>
					<th class="number">PO Amount</th>
					<th class="number">Invoice Amount</th>
					<th class="number">Payment</th>
					<th></th>
			</tr>
				<c:forEach items="${supplierCharge}" var="sc" varStatus="st">
				<c:set var="i" value="${st.index+1}"/>
				<c:set var="poPrint" value="${not empty sc.po_no && sc.po_no != null && sc.po_no != ''}"/>
			<tr class="${st.index == 0 ? 'firtsRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
				onclick="showToolbar(${st.index}, event, 'resultTable', {poNo: '${sc.po_no}',	invoiceType:'${sc.invoice_type}'}, [${poPrint}])"	onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
				<td>
					<input type="checkbox" name="paymentCheckBox" value="${i}" onclick="editSupplierPayment(this,${i})" />
					<input type="hidden" name="_accountGroup" value="${sc.account_group}"/>
					<input type="hidden" name="consignment_status" value="${sc.consignment_status}"/>
					<input type="hidden" name="_issueId" value="${sc.issue_id}"/>
					<input type="hidden" name="_centerId" value="${sc.center_id}" />
				</td>
				<td>
					<label name="poNo">${sc.po_no}</label>
						<input type="hidden" name="_poNo" value="${sc.po_no}"/>
						<input type="hidden" name="_invoiceType" value="${sc.invoice_type}"/>
				</td>
				<td>
						${sc.invoice_no}
						<input type="hidden" name="_invoice_no" value="${sc.invoice_no}"/>
				</td>
				<td>
						<fmt:formatDate value="${sc.invoice_date}" pattern="dd-MM-yyyy"/>
						<input type="hidden" name="_invoice_date" value="${sc.invoice_date}"/>
				</td>
				<td>
						<fmt:formatDate value="${sc.due_date}" pattern="dd-MM-yyyy"/>
				</td>
				<td>
					<label id="labelDescription"></label>
					<input type="hidden" name="_sdescription" value="" maxlength="50"/>
				</td>
				<td class="number">
						${sc.po_total}<input type="hidden" name="_poAmount" value="${sc.po_total}" />
				</td>
				<td class="number">${sc.final_amt}
					<input type="hidden" name="_grnAmount" value="${sc.final_amt}"  />
				</td>
				<td class="number">
						${sc.final_amt}
						<input type="hidden" name="_pendingAmount"
						value='<fmt:formatNumber pattern="0.00" value="${sc.final_amt}"/>'/>
						<input type="hidden" name="_paidAmount" />
				</td>
				<td>
					<a name="_editAnchor" id="editAnchor${i}" href="javascript:Edit" onclick="return showDescriptionDialog(this);"
						title="Edit Item Details">
						<img src="${cpath}/icons/Edit.png" class="button noToolbar" />
					</a>
				</td>
			</tr>
		</c:forEach>
	</table>
	<c:if  test="${param._method != 'getSupplierPaymentScreen'}">
			<insta:noresults hasResults="${not empty supplierCharge}"/>
	</c:if>
	<c:if test="${not empty supplierCharge}">
	<table width="100%" style="padding-top: 10">
		<tr>
			<td>
				<div style="float:left">
					<input type="radio" name="_selectItems" id="singleItem" value="item" checked="true"
					onclick="onCheckRadio(this.value)">Select Single Item
				</div>

				<div style="float:left">
					<input type="radio" name="_selectItems" id="pageItems" value="pgItems"
					onclick="onCheckRadio(this.value)">Select Page Items
				</div>
				<div style="float:left">
					<input type="radio" name="_selectItems" id="allItems" value="all" onclick="onCheckRadio(this.value);">Select All Items
				</div>
				<div style="float: right;">
					Items Total: <label id="allTotAmt" style="font-weight: bold">0</label>
				</div>
			</td>
		</tr>
	</table>
	<div class="screenActions" style="margin-bottom:10px">
		<input type="button" name="save" value="Save" onclick="return saveCharges();" />
	</div>
</c:if>
</div>

<dl class="accordion" id="viewContentDiv" style="padding-top:15px">
	<dt><span class="clrboth">View Posted Payments</span></dt>
	<dd class="${not empty supplierPayment ? 'open': ''}" style="width:952px">
		<div class="bd">
			<insta:paginate curPage="${supplierPaymentList.pageNumber}" numPages="${supplierPaymentList.numPages}" pageNumParam="_paymentPageNum" totalRecords="${supplierPaymentList.totalRecords}"/>
				<div class="resultList" >
					<table class="resultList" cellpadding="0" cellspacing="0" width="100%" id="paidPaymentTable"
						onmouseover="hideToolBar()">
						<tr	onmouseover="hideToolBar()">
								<th style="width: 5em;">
										<input type="checkbox" name="deleteAll" id="deleteAll" onclick="deleteAllCharges();">
											Delete
								</th>
								<th>PO No</th>
								<th>Invoice No</th>
								<insta:sortablecolumn name="invoice_date" title="${invoicedate}" tooltip="${invoicedate}"/>
								<th>Due Date</th>
								<th style="width: 10em;">Description</th>
								<th class="number">PO Amount</th>
								<th class="number">Invoice Amount</th>
								<th class="number">Payment </th>
						</tr>
						<c:forEach items="${supplierPayment}" var="sp" varStatus="st">
						<c:set var="j" value="${st.index+1}"/>
						 <c:set var="poPrint" value="${not empty sp.po_no && sp.po_no != null && sp.po_no != ''}"/>
						<tr class="${st.index == 0 ? 'firstRow': ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index+1}${j}, event, 'paidPaymentTable', {poNo: '${sp.po_no}',
								invoiceType:'${sc.invoice_type}'},[${poPrint}])" onmouseover="hideToolBar('${st.index}')"
								id="toolbarRow${st.index+1}${j}">
								<td>
										<input type="checkbox" name="paidCheckBox" value="${j}" id="paidCheckBox${1}"
											onclick="deleteSupplierPayment(this,${j});"/>
											<input type="hidden" name="_delConsignment_status" value="${sp.consignment_status}"/>
											<input type="hidden" name="_delIssueId" value="${sp.issue_id}"/>
								</td>
								<c:choose>
										<c:when test="${sp.invoice_type == 'I'}">
											<td>
													<label name="poNo">${sp.po_no}</label>
											</td>
										</c:when>
										<c:otherwise>
											<td>
													<label name="delPoNo">${sp.po_no}</label>
											</td>
													<input type="hidden" name="_delPoNo" value="${sp.po_no}" />
													<input type="hidden" name="_delPaymentId" value="${sp.payment_id}"/>
													<input type="hidden" name="_delinvoiceType" id="_delinvoiceType${j}"
															value="${sp.invoice_type}"/>
										</c:otherwise>
								</c:choose>
								<td>${sp.invoice_no}
										<input type="hidden" name="_delGrnNo" value="${sp.invoice_no}" />
								</td>
								<td>
										<fmt:formatDate value="${sp.invoice_date}" pattern="dd-MM-yyyy"/>
										<input type="hidden" name="_delGrnDate" value="${sp.invoice_date}"/>
								</td>
								<td>
										<fmt:formatDate value="${sp.due_date}" pattern="dd-MM-yyyy"/>
								</td>
								<td style="width: 10em;">${sp.description}
										<input type="hidden" name="_delDescription" value="${sp.description}" />
								</td>
								<td class="number">${sp.po_total}
										<input type="hidden" name="_delPoAmount" value="${sp.po_total}"/>
								</td>
								<td class="number">
										<fmt:formatNumber pattern="0.00" value="${sp.final_amt}"/>
										<input type="hidden" name="_delpaidAmount" value="${sp.final_amt}"/>
								</td>
								<td class="number">
										<fmt:formatNumber pattern="0.00" value="${sp.amount}"/>
										<input type="hidden" name="_delPendingAmount" value="${sp.amount}" />
								</td>
						</tr>
						</c:forEach>
					</table>
					<c:if test="${param._method != 'getSupplierPaymentScreen'}">
							<insta:noresults hasResults="${not empty supplierPayment}"/>
					</c:if>
					<c:if test="${not empty supplierPayment}">
					<table width="100%" style="padding-top:10px">
						<tr>
							<td>
								<div class="screenActions" style="float:left">
									<input type="button" property="delete" value="Delete"
									onclick="return deleteCharges()"/>
								</div>
								<div style="float: right">
									Total Amount : <label id="totalAmount" style="font-weight: bold">${totalPaidAmount}</label>
								</div>
							</td>
						</tr>
					</table>
					</c:if>
				</div>
		</div>
	</dd>
</dl>
</form>
<script>
		var suppliersList = ${supplierList};
</script>
</body>
</html>

