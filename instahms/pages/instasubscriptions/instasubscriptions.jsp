<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.chargebee.models.Invoice"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"  %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>


<jsp:useBean id="currentDate" class="java.util.Date"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>


<html>
	<head>
		<title>Insta Subscriptions - Insta HMS</title>
		<meta http-equiv="Content Type" content="text/html; charset=iso-8859-1"/>
		<meta name="i18nSupport" content="true"/>
		<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
		<insta:link file="instasubscriptions/instasubscriptions.js" type="js"/>
		<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
		<insta:link type="css" file="instasubscriptions/instasubscriptions.css" />
	</head>
	<body onload="init()">
		<h1 style="margin: 15px 0px">
			Insta Subscriptions
		</h1>
		<insta:feedback-panel></insta:feedback-panel>
		<jsp:useBean id="Date" class="java.util.Date"/>
		<c:choose>
			<c:when test="${error == null}">
				<c:forEach items="${customerSubscriptionList}" var="subscription">			
					<div class="subscriptionDiv1"> 
						<div class="subscriptionHeaderCommon subscriptionName">
							<span >Subscription Name:&nbsp;</span>
							<c:set var="subId" value="${subscription.get('id')}"></c:set>
							<span class="boldInfo">${planMap[subId].get('name')}</span>
						</div>
						<div class="subscriptionHeaderCommon subscriptionId">
							<span >ID:&nbsp;</span>
							<span class="boldInfo">${subscription.get('id')}</span>
						</div>
						<div class="subscriptionHeaderCommon subscriptionStatus">
							<span >Status:&nbsp;</span>
							<span class="boldInfo activeColor">Active</span>
						</div>
						<div class="subscriptionHeaderCommon subscriptionRenewal">
							<span >Renewal Date:&nbsp;</span>
							<jsp:setProperty name="Date" property="time" value="${subscription.get('current_term_end')*1000}"/>
							<fmt:formatDate value="${Date}" var="nextrenewdate" pattern="dd-MM-yyyy"/>
							<span class="boldInfo">${nextrenewdate}</span>
						</div>
					</div>
					<c:set var="invoiceCount" value="${fn:length(invoiceMap[subId])}"></c:set>
					<c:set var="invoiceCounter" value="0"></c:set>
					<c:forEach items="${invoiceMap[subId]}" var="invoice">
						<c:set var="invoiceCounter" value="${invoiceCounter + 1}"></c:set>
						<div class="invoiceDetailsDiv1 ${invoiceCounter == invoiceCount ? 'invoiceDetailsDiv2' : ''}">
							<div class="invoiceDetailsCommon invoiceId">
								<div class="invoiceDetailsLabel">INVOICE ID</div>
								<span class="boldInfo">${invoice.get('id')}</span>
							</div>
							<div class="invoiceDetailsCommon invoiceDate">
								<div class="invoiceDetailsLabel">DATE</div>
								<jsp:setProperty name="Date" property="time" value="${invoice.get('date')*1000}"/>
								<fmt:formatDate value="${Date}" var="invoiceDate" pattern="dd-MM-yyyy"/>
								<span class="boldInfo">${invoiceDate}</span>
							</div>
							<div class="invoiceDetailsCommon invoiceTo">
								<div class="invoiceDetailsLabel">INVOICE TO</div>
								<c:set var="customerName" value=""/>
								<c:choose >
									<c:when test="${invoice.has('billing_address') && invoice.get('billing_address').has('first_name')}">
										<c:set var="customerName"
											value="${invoice.get('billing_address').get('first_name')}&nbsp;${invoice.get('billing_address').has('last_name') ? invoice.get('billing_address').get('last_name') : ''}" />
									</c:when>
									<c:when test="${invoice.has('billing_address') && invoice.get('billing_address').has('company')}">
										<c:set var="customerName" value="${invoice.get('billing_address').get('company')}" />
									</c:when>
									<c:otherwise>
										<c:set var="customerName" value="${invoice.get('customer_id')}" />
									</c:otherwise>
								</c:choose>
								<span class="boldInfo">${customerName}</span>
							</div>
							<div class="invoiceDetailsCommon invoiceAmount">
								<div class="invoiceDetailsLabel">INVOICE AMT</div>
								<span class="boldInfo">${invoice.get('currency_code')}&nbsp;${invoice.get('total')/100}</span>
							</div>
							<div class="invoiceDetailsCommon invoicePaidAmount">
								<div class="invoiceDetailsLabel">PAID AMT</div>
								<span class="boldInfo">${invoice.get('currency_code')}&nbsp;${invoice.get('total')/100 - invoice.get('amount_due')/100}</span>
							</div>
							<div class="invoiceDetailsCommon invoiceDueStatus">
								<div class="invoiceDetailsLabel">STATUS</div>
								<div class="boldInfo invoiceDueInfo">DUE</div>
							</div>
							<div class="invoiceDetailButtonsDiv">
								<a href="${cpath}/instasubscriptions/InstaSubscriptions.do?_method=getInvoicePdf&invoiceId=${invoice.get('id')}">
									<input class="invoiceDownload cursorOnHover" type="button" value="Download">
								</a>
								<c:set var="currency_code" value="${invoice.get('currency_code')}"></c:set>
								<input class="invoiceRecordPayment cursorOnHover" onclick="openRecordPaymentDialog(${invoice.get('id')}, ${invoice.get('amount_due')}, '${invoice.get('currency_code')}')" value="Record Payment" type="button" />
							</div>
						</div>
					</c:forEach>
					<c:choose>
						<c:when test="${invoiceCount == 0}">
							<div class="invoiceNoDue">
								<span>No Invoices are due</span>
							</div>
						</c:when>
					</c:choose>
				</c:forEach>
				<div class="subscriptionDiv1">
					<div class="subscriptionHeaderCommon">
							<span >Other Invoices&nbsp;</span>
					</div>
				</div>
				<c:set var="invoiceCount" value="${fn:length(otherInvoiceList)}"></c:set>
				<c:set var="invoiceCounter" value="0"></c:set>
				<c:forEach items="${otherInvoiceList}" var="invoice">
						<c:set var="invoiceCounter" value="${invoiceCounter + 1}"></c:set>
						<div class="invoiceDetailsDiv1 ${invoiceCounter == invoiceCount ? 'invoiceDetailsDiv2' : ''}">
							<div class="invoiceDetailsCommon invoiceId">
								<div class="invoiceDetailsLabel">INVOICE ID</div>
								<span class="boldInfo">${invoice.get('id')}</span>
							</div>
							<div class="invoiceDetailsCommon invoiceDate">
								<div class="invoiceDetailsLabel">DATE</div>
								<jsp:setProperty name="Date" property="time" value="${invoice.get('date')*1000}"/>
								<fmt:formatDate value="${Date}" var="invoiceDate" pattern="dd-MM-yyyy"/>
								<span class="boldInfo">${invoiceDate}</span>
							</div>
							<div class="invoiceDetailsCommon invoiceTo">
								<div class="invoiceDetailsLabel">INVOICE TO</div>
								<c:set var="customerName" value=""/>
								<c:choose >
									<c:when test="${invoice.has('billing_address') && invoice.get('billing_address').has('first_name')}">
										<c:set var="customerName"
											value="${invoice.get('billing_address').get('first_name')}&nbsp;${invoice.get('billing_address').has('last_name') ? invoice.get('billing_address').get('last_name') : ''}" />
									</c:when>
									<c:when test="${invoice.has('billing_address') && invoice.get('billing_address').has('company')}">
										<c:set var="customerName" value="${invoice.get('billing_address').get('company')}" />
									</c:when>
									<c:otherwise>
										<c:set var="customerName" value="${invoice.get('customer_id')}" />
									</c:otherwise>
								</c:choose>
								<span class="boldInfo">${customerName}</span>
							</div>
							<div class="invoiceDetailsCommon invoiceAmount">
								<div class="invoiceDetailsLabel">INVOICE AMT</div>
								<span class="boldInfo">${invoice.get('currency_code')}&nbsp;${invoice.get('total')/100}</span>
							</div>
							<div class="invoiceDetailsCommon invoicePaidAmount">
								<div class="invoiceDetailsLabel">PAID AMT</div>
								<span class="boldInfo">${invoice.get('currency_code')}&nbsp;${invoice.get('total')/100 - invoice.get('amount_due')/100}</span>
							</div>
							<div class="invoiceDetailsCommon invoiceDueStatus">
								<div class="invoiceDetailsLabel">STATUS</div>
								<div class="boldInfo invoiceDueInfo">DUE</div>
							</div>
							<div class="invoiceDetailButtonsDiv">
								<a href="${cpath}/instasubscriptions/InstaSubscriptions.do?_method=getInvoicePdf&invoiceId=${invoice.get('id')}">
									<input class="invoiceDownload cursorOnHover" type="button" value="Download">
								</a>
								<input class="invoiceRecordPayment cursorOnHover" onclick="openRecordPaymentDialog(${invoice.get('id')}, ${invoice.get('amount_due')}, '${invoice.get('currency_code')}')" value="Record Payment" type="button" />
							</div>
						</div>
				</c:forEach>
				<c:choose>
					<c:when test="${invoiceCount == 0}">
						<div class="invoiceNoDue">
							<span>No Invoices are due</span>
						</div>
					</c:when>
				</c:choose>
				<div class="paymentHistory">
					<span>Payment History</span>
				</div>
				<div class="pagination">
					<div id="previousPageDiv" style="float:left;">
						<span class="noFurtherPages" id="previousPage" onclick="">
							&lt;&nbsp;Prev&nbsp;|
						</span></div>
					<div id="nextPageDiv" style="float:right;">
						<c:choose>
							<c:when test="${next == false}">
								<span id="nextPage" class="noFurtherPages" onclick="">&nbsp;Next&nbsp;&gt;</span>
							</c:when>
							<c:otherwise>
								<span id="nextPage" class="cursorOnHover" onclick="paidInvoiceList(${page +1})">&nbsp;Next&nbsp;&gt;</span>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
				<table class="detailList" id="paymentTable">
					<tr>
						<th class="paymentHistoryHeader paymentInvoiceId">Invoice ID</th>
						<th class="paymentHistoryHeader paymentDate">Date</th>
						<th class="paymentHistoryHeader paymentInvoiceAmount">Invoice Amount</th>
						<th class="paymentHistoryHeader paymentPaidAmount">Paid Amount</th>
						<th class="paymentHistoryHeader paymentPaidDate">Paid Date</th>
						<th class="paymentHistoryHeader paymentStatus">Status</th>
						<th class="paymentHistoryHeader paymentDownload"></th>
					</tr>
					<c:forEach items="${paidInvoiceList}" var="invoice">
						<tr>
							<td class="paymentHistoryHeader paymentInvoiceId">${invoice.get('id')}</td>
							<td class="paymentHistoryHeader paymentDate">
								<jsp:setProperty name="Date" property="time" value="${invoice.get('date')*1000}"/>
								<fmt:formatDate value="${Date}" var="paidinvoiceDate" pattern="dd-MM-yyyy"/>
								${paidinvoiceDate}
							</td>
							<td class="paymentHistoryHeader paymentInvoiceAmount">${invoice.get('total')/100}&nbsp;${invoice.get('currency_code')}</td>
							<td class="paymentHistoryHeader paymentPaidAmount">${invoice.get('amount_paid')/100}&nbsp;${invoice.get('currency_code')}</td>
							<td class="paymentHistoryHeader paymentPaidDate">
								<jsp:setProperty name="Date" property="time" value="${invoice.get('paid_at')*1000}"/>
								<fmt:formatDate value="${Date}" var="paidinvoiceDate" pattern="dd-MM-yyyy"/>
								${paidinvoiceDate}
							</td>
							<td class="paymentHistoryHeader paymentStatus"><div class="invoicePaidInfo">PAID</div></td>
							<td class="paymentHistoryHeader paymentDownload">
								<a href="${cpath}/instasubscriptions/InstaSubscriptions.do?_method=getInvoicePdf&invoiceId=${invoice.get('id')}">
									<input class="invoiceDownload cursorOnHover" type="button" value="Download">
								</a>
							</td>
						</tr>
					</c:forEach>
				</table>
				
				<div id="addRecordDialog" style="display: none;width:350px">
		    		<div class="bd">
		    		<fieldset class="fieldSetBorder">
		                <legend class="fieldSetLabel">Record Payment</legend>
		                <form action="${cpath}/instasubscriptions/InstaSubscriptions.do?_method=payCustomerInvoice" id="recordPaymentForm" method="Post">
		    				<input id="recordInvoice" name="recordInvoice" type="hidden">
			    		 <table class="formtable">
			    		 	<tr> 
			    		 		<td class="forminfo"> Amount <span style="color:red">*</span></td>
								<td>
									<div class="addOnLabel"><span id="currencyAddOn"></span></div>
									<div class="recordAmt">
										<input name="recordAmount" id="recordAmount" type="number" style="width:108px;border:0px;height:20px"/>
									</div>
								</td>
			    		 	</tr>
			    		 	<tr> 
						 		<td class="forminfo"> Payment Method <span style="color:red">*</span></td>
			    		 		<td>  
						 			<select id="paymentMethod" name="paymentMethod" style="width:140px;" onChange="resetReferenceNo()">
			    		 				  <option value="">-- Select --</option>
										  <option value="bank_transfer">Bank Transfer</option>
										  <option value="cash">Cash</option>
										  <option value="check">Cheque</option>
										  <option value="other">Other</option>
									</select> 
								</td>
			    		 	</tr>
			    		 	<tr> 
								<td class="forminfo"> Payment Date <span style="color:red">*</span></td>
			    		 		<td> 
			    		 			<insta:datewidget name="paymentDate" id="paymentDate" btnPos="left" style="width:120px;"/>
			    		 		</td>
			    		 	</tr>
			    		 	<tr>
								<td class="forminfo"> Reference No </td>
			    		 		<td> <input name="refNo" id="refNo" type="text" style="width:140px;"/></td>
			    		 	</tr>
			    		 	<tr> 
								<td class="forminfo"> Comments </td>
			    		 		<td> <textarea id="comments" name="comments" rows="2" style="width:140px;resize: none;"></textarea></td>
			    		 	</tr>
			    		 </table>
			    		 </form>
		    		 </fieldset>
		    		 <button id="Record"> Save </button> <button id="Close"> Cancel </button>
		    		</div>
		    	</div>
	    	</c:when>
	    </c:choose>
	</body>
</html>