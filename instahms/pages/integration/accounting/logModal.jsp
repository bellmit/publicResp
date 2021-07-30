<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<spring:url value="/assets/css/styles.css" var="mainCss" />
<spring:url value="/assets/js/jquery-3.1.1.js" var="jqueryJs" />
<spring:url value="/assets/fonts/CamphorPro-Regular.ttf" var="regularFont" />
<spring:url value="/assets/fonts/CamphorPro-Bold.ttf" var="boldFont" />
<spring:url value="/assets/js/semantic-2.2.13.min.js" var="semanticJs" />
<spring:url value="/assets/js/script.js" var="scriptJs" />
<spring:url value="/assets/css/semantic-2.2.13.min.css" var="semanticCss" />
<spring:url value="/assets/css/datepicker.css" var="datepickerCss" />
<spring:url value="/assets/js/datepicker.js" var="datepickerJs" />

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Practo</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no">
<meta charset="utf-8"/>
<link href="${regularFont}" rel="stylesheet" />
<link href="${boldFont}" rel="stylesheet" />
<link href="${mainCss}" rel="stylesheet" />
<script src="${jqueryJs}"></script>
<script src="${semanticJs}"></script>
<script src="${scriptJs}"></script>
<link href="${semanticCss}" rel="stylesheet" />
<link href="${datepickerCss}" rel="stylesheet" />
<script src="${datepickerJs}"></script>
</head>
<body>

<div class="main-wrapper">
	<!-- Header section starts -->
	<div class="main-header ui items">
		<div class="add-link">
		<i class="sidebar icon hamburger-menu"></i>
			<!-- <a id='credential-modal'> Add Zoho Account </a>
			<span class="header-span"> | </span>
			<a  href="tasks.html"> All Sync Requests </a> -->
			<ul class="ui right header-ul">
				<button class="home-btn">
					<i class="home icon header-icons"></i> Home
				</button>
				<li>
					<a class="home-btn" th:href="@{/logModal}"><i class="info icon header-icons custom-head-icon"></i> Export Logs</a>
				</li>
				<li>
					<a class="home-btn" id="toast-danger"><i class="window close icon header-icons custom-head-icon"></i> Error sample </a>
				</li>
				<li>
					<a class="home-btn" href="#"><i class="help icon header-icons"></i> Help</a>
				</li>
				<li>
					<a class="home-btn" ><i class="user icon header-icons"></i> User</a>
				</li>
			</ul>
		</div>
	</div>
   </div>
   
   <div class="ui fullscreen modal audit-log-modal">
		<a class="close pad0 modal-close" href="/practo">Close</a>
			<div class="header">
				<h3 class="ui header">Audit Log</h3>
			</div>
			<div class="ui grid padded scrolling content custom-content">
				<div class="ui row fluid icon input stackable pad0">
				
				<c:url var="pst_url"  value="/logModal" />
				<form:form id="formdata" action="${pst_url}" object="${logg}" modelAttribute="logg" method="post">
					<div class="three wide column ui input pad-l0">
						<label class="filter-label" style="padding: 10px 0;">Date (From)</label>
						<form:input type="text" placeholder="dd/mm/yyyy" class="header-inputs-mobile datepicker fd" path="fromDate"/>
					</div>
					<div class="three wide column ui input">
						<label class="filter-label" style="padding: 10px 0;">Date (To)</label>
						<form:input type="text" placeholder="dd/mm/yyyy" class="header-inputs-mobile datepicker td" path="endDate"/>
					</div>
				</form:form>
				</div><br><br>
				<c:if test="${auditLogError != '' && isError}">
				<div class="ui message message-container">
						  <i class="close icon"></i>
						  <span >${auditLogError}</span>
						</div>
				</c:if>
				<c:if test="${logdata.size() > 0}">
					<div class="ui sixteen wide column task-details-section pad0">
						<c:forEach items="${logdata}" var="sub">
							<div class="details-block pad0">
								 <div class="ui styled fluid accordion">
								 	<div class="title">
								 		<i class="dropdown icon"></i>
								 		<div class="details-heading d-iblock">
								 			<span class="modal-details-title">Export Date & Time: </span>
								 			<label>
								 				<c:out value="${sub.getValue()[0].get('sync_date') + ' ,'} " /> ,
								 			</label>
											<span class="modal-details-title">Export Status: </span><label> Success ,</label>
											<span class="modal-details-title">No. of Vouchers: </span>
											<label">
												<c:out value="${sub.getValue().size() + ' ,'}" /> ,
											</label>
											<span class="modal-details-title">User: </span><label> InstaAdmin</label>
								 		</div>
								 	</div>
								 	
								 	<div class="content">
									    <div class=" summary-table">
											<table class="ui unstackable table lgg">
												<thead>
													<tr>
														<th>Id</th>
														<th>Voucher Type</th>
														<th>Voucher No.</th>
														<th>Other Details</th>
														<th>Status</th>
														<th>Particulars</th>
														<th >Reference Details:</th>
														<th class="text-right">Debit</th>
														<th class="text-right">Credit</th>
													</tr>
												</thead>
												<tbody>
													<c:forEach items="${sub.getValue()}" var="res">
													<tr>
														<td>
															<c:out value="${res.get('group_id')}" />
														</td>
														<td >
															<c:out value="${res.get('voucher_type')}" />
														</td>
														<td >
															<c:out value="${res.get('voucher_id')}" />
														</td>
														<td>
															<c:out value="${'Bill No.' + res.get('voucher_id') + '(MR No. MR000021: Child of Tejaswini Singh)'}" />
														</td>
														<td>
															<c:out value="${res.get('sync_status')}" />
														</td>
														<td>
															<c:out value="${res.get('credit_account')}" />
														</td>
														<td >
															<c:out value="${res.get('voucher_id') + ', Child of Tejaswini Singh'}" /> 
														</td>
														<td class="text-left" >
															<c:out value="${res.get('account_type') == 'debit'}? ${res.get('net_amount')} : '0.00'" />
														</td>
														<td class="text-left">
															<c:out value="${res.get('account_type') == 'credit'}? ${res.get('net_amount')} : '0.00'" />
														</td>
													</tr>	
													</c:forEach>													
												</tbody>
											</table>
										</div>
									</div>
								 
								 </div>
							
							</div>
						
						</c:forEach>
					
					</div>
				</c:if>
			</div>
		<div class="actions">
			<a href="/practo" class="ui button yellow">Ok</a>
		</div>
	</div>

</body>
</html>