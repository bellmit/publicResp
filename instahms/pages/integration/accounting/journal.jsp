<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%-- 
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
--%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
 
<html>
<head>
<insta:link type="css" file="fa.css" var="mainCss" />
<insta:link type="script" file="fa/jquery-3.1.1.js"/>
<%-- 
<spring:url value="/assets/fonts/CamphorPro-Regular.ttf" var="regularFont" />
<spring:url value="/assets/fonts/CamphorPro-Bold.ttf" var="boldFont" />
--%>
<insta:link type="script" file="fa/semantic-2.2.13.min.js"/>
<insta:link type="script" file="fa.js" />
<insta:link type="css" file="fa/semantic-2.2.13.min.css"/>
<insta:link type="css" file="fa/datepicker.css"/>
<insta:link type="script" file="fa/datepicker.js"/>
	

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Accounting Export - Insta HMS</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no">
	<meta charset="utf-8"/> 
	
	<%-- <link th:href="<c:url value="/assets/fonts/CamphorPro-Regular.ttf" />" rel="stylesheet">
	<link th:href="<c:url value="/assets/fonts/CamphorPro-Regular.ttf" />"  rel="stylesheet">
	<script type="text/javascript" th:src="<c:url value="/assets/js/jquery-3.1.1.js" />"></script>
	<link rel="stylesheet" type="text/css" th:href="<c:url value="/assets/css/styles.css" />"> --%>
	
	<%--
	/*
	Converted spring:url to insta:link which will include the javascript and the styles and 
	hence these relstylesheet references are not required anymore
	*/  
	--%> 
<!--	
	<link href="${regularFont}" rel="stylesheet" />
	<link href="${boldFont}" rel="stylesheet" />
	<link href="${mainCss}" rel="stylesheet" />
-->
<!-- 
    <script src="${jqueryJs}"></script>
    <script src="${semanticJs}"></script>
    <script src="${scriptJs}"></script>
    <link href="${semanticCss}" rel="stylesheet" />
    <link href="${datepickerCss}" rel="stylesheet" />
    <script src="${datepickerJs}"></script>
 -->
</head>
<body>
<c:set var="hasResult" value="${not empty result}"/>
<c:set var="vtypes" value="${referenceData.voucher_types}"/>
<c:set var="agroups" value="${referenceData.account_groups}"/>
<div class="main-wrapper">
		<!-- Header section starts -->
		<%-- 
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
						<a class="home-btn"><i class="info icon header-icons custom-head-icon"></i> Export Logs</a>
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
		--%>
		<!-- Header section ends -->
		<c:if test="${not empty exportMessage}">
			<c:set var="exportMessageClass" value="export-errored" />
			<c:set var="exportTitle" value="Accounting Export Failed." />
			<c:set var="exportSymbol" value="toast-error" />
			<c:if test="${not (exportStatus eq 'notify' || (exportStatus eq 'error') )}">
				<c:set var="exportMessageClass" value="export-success" />
				<c:set var="exportTitle" value="Accounting Export Success" />
				<c:set var="exportSymbol" value="toast-success" />
			</c:if>
			<div class="ui padded grid">
				<div id="snackbar" class="pad0 ${exportMessageClass}">
						<div class="ui row">
						<img width="22" height="22" class="notification-icon" src="<%=request.getContextPath()%>/images/${exportSymbol}.svg">
						<h6 class="d-iblock ui fourteen wide column mar0 toast-head">${exportTitle}</h6>
						<i class="delete icon close-icon" id="close-toast"></i>
					</div>
				<div class="toast-para">
					<p>${exportMessage}</p>
				</div>
			</div>
			</div>
		</c:if>

		<div class="ui padded grid">
			<div id="date-range-snackbar" class="pad0">
				<div class="ui row date-range-error-container">
				<img width="22" height="22" class="notification-icon" src="<%=request.getContextPath()%>/images/toast-error.svg">
				<h6 class="d-iblock ui fourteen wide column mar0 toast-head date-range-error-message">Please select a valid date range</h6>
				<i class="delete icon close-icon" id="close-data-range-toast"></i>
				</div>
				</div>
			</div>
		</div>
		<!-- Message container - removed temporarily as it was causing the UI to wrap --> 
		<%-- <c:if test="${not empty exportMessage}">
			<c:set var="errorStyle" value="${not empty exportStatus && (exportStatus eq 'error') ? 'background-color:red' : ''}"/>

			<c:set var="errorStyle" value="${not empty exportStatus && (exportStatus eq 'notify') ? 'color:red' : ''}"/>

			<div class="ui message message-container" style="width:100%;${errorStyle};">
			  <i class="close icon"></i>
			  <span >${exportMessage}</span>
			</div>
		</c:if> --%>
		<!-- Main wrapper starts -->
		<div class="main-wrapper">
			<div class="ui top attached tabular menu">
				<div class="active item custom-tab" style="border-radius: 0px !important;">Accounting Export</div>
			</div>
			<div class="search-filter">
				<p class="sub-title">Financial Accounting</p>
			</div>
			
			<!-- Search filter section starts -->
			<div class="filter-section" id="stickyfilter">
				<div class="search-header">
					<label class="thick-label-medium">SEARCH RECORDS</label>
					<button class="toggle-search button"><span id="toggle-label">Hide</span></button>
				</div>
				
				<div class="search-body">
					<c:url var="search_url"  value="${cpath}/billing/accounting/search.htm" />
					<%--
					<form:form action="${search_url}" object="${search}" modelAttribute="search" method="post">
					 --%>
					<form action="${search_url}" method="GET" onsubmit="return validateSearchForm();">
						<div class="ui doubling stackable grid filter-bar" style="margin: 0px;">
							<div class="two wide column ui input pad-l0">
								<label class="filter-label">Voucher Date (From)</label>
								<input type="hidden" name="voucher_date@op" value="ge,lt"/>
								<input type="text" placeholder="dd-mm-yyyy" class="header-inputs-mobile  datepicker" name="voucher_date"
									id="voucher_date_0" value="${paramValues.voucher_date[0]}"/>
							</div>
							<div class="two wide column ui input">
								<label class="filter-label">Voucher Date (To)</label>
								<input type="text" placeholder="dd-mm-yyyy" class="header-inputs-mobile  datepicker" name="voucher_date"
									id="voucher_date_1" value="${paramValues.voucher_date[1]}"/>
							</div>
							<div class="two wide column ui input">
								<label class="filter-label">Center / Acc. Group </label>
								<input type="hidden" name="center_id@type" value="integer"/>
								<input type="hidden" name="d_account_group@type" value="integer"/>
								<select class="ui compact selection dropdown filter-section-select header-inputs-mobile" name="_account_id">
									<%--
									<option value="">- - select - -</option>
									--%>
									<c:if test="${not empty agroups}">
									<c:forEach var="agroup" items="${agroups}" varStatus="agroupIndex">
									<%--
										<c:set var="agroupAttr" value=""/>
										<c:if test="${agroup.type eq 'C' && agroup.ac_id eq paramValues.center_id[0]}">
											<c:set var="agroupAttr" value="selected='true'"/>
										</c:if>
										<c:if test="${agroup.type eq 'A' && agroup.store_center_id eq paramValues.center_id[0]}">
											<c:set var="agroupAttr" value="selected='true'"/>
										</c:if>
										<option value="${agroup.type eq 'C' ? agroup.ac_id : agroup.store_center_id}" ${agroupAttr}>${agroup.ac_name}</option>
									--%>
										<c:set var="agroupAttr" value=""/>
										<c:if test="${agroup.id eq paramValues._account_id[0]}">
											<c:set var="agroupAttr" value="selected='true'"/>
										</c:if>
										<option value="${agroup.id}" ${agroupAttr}>${agroup.ac_name}</option>
									</c:forEach> 
									</c:if>
								</select>
							</div>
							<div class="four wide column voucher-type">
								<label class="filter-label">Voucher Type</label>
								<%-- use a distinct separator so that when one voucher type is a substring of another, they do not get mixed up
								Separator at the end is necessary to keep the search string same for setting the selected values--%>
								<c:set var="voucher_type_string" value="${fn:join(paramValues.voucher_type, '##')}##" />
								<select class="ui fluid search select dropdown" id="voucher-types" multiple="multiple" name="voucher_type">
									<option value="">- - All - -</option>
									<c:if test="${not empty vtypes}">
									<c:forEach var="vtype" items="${vtypes}" varStatus="vtypeIndex">
										<c:set var="vtypeAttr" value=""/>
										<c:set var="vtypeKey" value="${vtype.key}##"/>
										<c:if test="${fn:contains(voucher_type_string, vtypeKey)}">
											<c:set var="vtypeAttr" value="selected='true'"/>
										</c:if>
										<option value="${vtype.key}" ${vtypeAttr}>${vtype.value}</option>
									</c:forEach> 
									</c:if>
								</select>
							</div>
							<div class="two wide column ui input">
								<label class="filter-label">Export Status</label>
								<input name="update_status@type" value="integer" type="hidden"/>
								<select class="ui compact selection dropdown filter-section-select header-inputs-mobile" name="update_status">
									<c:set var="statusSelAttr" value=""/>
									<c:if test="${paramValues.update_status[0] == 0}">
										<c:set var="statusSelAttr" value="selected='true'"/>
									</c:if>
									<option value="0" ${statusSelAttr}>Open</option>
									<c:set var="statusSelAttr" value=""/>
									<c:if test="${paramValues.update_status[0] == 1}">
										<c:set var="statusSelAttr" value="selected='true'"/>
									</c:if>
									<option value="1" ${statusSelAttr}>Exported</option>
									<c:set var="statusSelAttr" value=""/>
									<c:if test="${paramValues.update_status[0] == -1}">
										<c:set var="statusSelAttr" value="selected='true'"/>
									</c:if>
									<option value="-1" ${statusSelAttr}>Failed</option>
								</select>
							</div>
							<div class="one wide column ui filter-buttons">
								<label class="filter-label"></label>
								<button type="submit" class="ui button basic">Search</button>
							</div>
						</div>
					</form>
				</div>			
			</div>
		<!-- Search filter section ends -->
		<c:url var="pst_url"  value="${cpath}/billing/accounting/export.htm" />
		<%-- <form:form method="post" action="${pst_url}" > --%>
		<form method="post" action="${pst_url}" >
			<div class="ui grid padded stackable details-summary-container">
				<div class="eleven wide column details-division">
				<!-- Details section starts -->
				<c:if test="${hasResult}">
					<input type="hidden" name="_account_id" value="${paramValues._account_id[0]}"/>
					<div class="details-section">
					  <label class="thick-label-medium details-label">VOUCHER DETAILS</label>
					    <div class="details-block">
					  		<c:set var="guidOpSet" value="false"/>
							<c:forEach var="entry" items="${result}">
							<c:forEach var="voucherTypeList" items="${entry.value}">
								<c:set var="prev_no" value=""/>
								<c:forEach var="bean" items="${voucherTypeList}" varStatus="loopStatus">
								<c:if test="${bean.map.voucher_no ne prev_no || loopStatus.first}">
								<div class="details-heading ui grid">
									<%--
									<c:if test="${bean.map.update_status != 1}">
									<input type="hidden" value="${bean.map.guid}" name="guid"/>
										<c:if test="${guidOpSet == 'false'}">
											<input type="hidden" value="in" name="guid@op"/>
											<c:set var="guidOpSet" value="true"/>
										</c:if>
									</c:if>
									--%>
									<div class="thirteen wide column">
										<span class="details-title"></span>
										<p class="table-head-label" >
										<fmt:formatDate pattern="dd-MM-yyyy" value="${bean.map.voucher_date}" />
										</p>
										
										<span class="details-title">Voucher Type: </span>									
										<p class="table-head-label" >
											<c:set var="vtype_desc" value=""/>
											<c:if test="${not empty bean.map.voucher_type}">
												<c:set var="vtype_desc" value="${bean.map.voucher_type}"/>
												<c:if test="${not empty vtypes}">
													<c:set var="vtypeKey" value="${fn:trim(bean.map.voucher_type)}"/>
													<c:set var="vtype_desc" value="${vtypes[vtypeKey]}"/>
												</c:if>
											</c:if>
											<c:out value="${vtype_desc}" />
										</p>
										
										<span class="details-title">Voucher No: </span>
										<p class="table-head-label" >
											<c:out value="${bean.map.voucher_no}" />
										</p>
										
										<c:if test="${not empty bean.map.remarks }">
										<span class="details-title ">Other Details: </span>
										<p class="details-truncate table-head-label" >
											<c:out value="${bean.map.remarks}"/>
										</p>
										</c:if>
									</div>
									<c:set var="popupPosition" value="top left" />
									<c:if test="${loopStatus.first}">
										<c:set var="popupPosition" value="bottom left" />
									</c:if>
									<div class="three wide column exported-status-container">
										<c:set var="status_msg" value=""/>
										<c:if test="${bean.map.update_status == 1 && not empty bean.map.last_export_date}"> 
											<div data-position="${popupPosition}" class="exported green-color"><fmt:formatDate pattern="dd-MM-yyyy HH:mm:ss" value="${bean.map.last_export_date}"/></div>
											<c:set var="status_msg" value="Successfully exported @ ${bean.map.last_export_date}"/>
										</c:if>
										<c:if test="${empty bean.map.update_status || bean.map.update_status == -1}">
											<div data-position="${popupPosition}" class="exported red-color"><fmt:formatDate pattern="dd-MM-yyyy HH:mm:ss" value="${bean.map.last_export_date}"/>
											</div>
											<c:set var="status_msg" value="Export Failed with reason : ${bean.map.last_status_message}"/>
										</c:if>
										<%-- popup on hover --%>
										<div class="ui fluid popup">
											<p> ${bean.map.voucher_no}, <fmt:formatDate pattern="dd-MM-yyyy HH:mm:ss" value="${bean.map.voucher_date}" /> 
											<br/>${status_msg}</p>
										</div>
										<%-- 
										popup on hover
										<div class="ui fluid popup">
											<c:set var="errMsg" value="Involved account types are not applicable"/>
											<p >${bean.map.voucher_no}, <fmt:formatDate pattern="dd-MM-yyyy HH:mm:ss" value="${bean.map.voucher_date}"/>
											<br/> Export failed with the reason : ${errMsg}</p>
										</div>
										 --%>
									</div>
								</div>
								</c:if>
								<div class="details-table summary-table">
									<table class="ui unstackable table">
										<c:if test="${bean.map.update_status != 1}">
										<%-- set up a guid variable only if the voucher is exportable --%>
										<input type="hidden" value="${bean.map.guid}" name="guid"/>
											<c:if test="${guidOpSet == 'false'}">
												<input type="hidden" value="in" name="guid@op"/>
												<c:set var="guidOpSet" value="true"/>
											</c:if>
										</c:if>
									
										<thead style="${(bean.map.voucher_no ne prev_no || loopStatus.first) ? 'visibility:visible;' : 'visibility:collapse;'}">
											<tr>
												<th class="header-column" width="40%">Particulars</th>
												<th class="header-column" width="30%">Reference Details</th>
												<th class="header-column text-right" width="15%">Debit</th>
												<th class="header-column text-right" width="15%">Credit</th>
											</tr>
										</thead>
										<tbody>
										<tr>
											<td> <c:out value="${bean.map.credit_account}" /></td>
											<td>${bean.map.voucher_ref}</td>
											<td class="text-right"><div >0.00</div></td>
											<td class="text-right"><div >${bean.map.net_amount}</div></td>
										</tr>
										<tr>
											<td> <c:out value="${bean.map.debit_account}" /></td>
											<td>${bean.map.voucher_ref}</td>
											<td class="text-right"><div >${bean.map.net_amount}</div></td>
											<td class="text-right"><div >0.00</div></td>
										</tr>
									</tbody>
									</table>
								</div>
								<c:set var="prev_no" value="${bean.map.voucher_no}"/>
							</c:forEach>
							</c:forEach>
							</c:forEach>
						</div>
					</div>
				</c:if>
					
				<!-- Details section ends -->
				</div>
				<!-- Summary section starts -->
				<c:if test="${hasResult}">
					<div class="five wide column summary-block">
						<label class="thick-label-medium summary-label">SUMMARY</label><br>
						<div class="summary-content">
							<c:forEach var="entry" items="${summary}">
							<label class="thick-label-medium summary-section">${entry.key}</label>
							<div class="summary-table">
								<table class="ui unstackable table">
									<thead>
										<tr>
											<th colspan="3">Account</th>
											<th class="text-right">Debit</th>
											<th class="text-right">Credit</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${entry.value}" var="account">
											<tr>
												<td colspan="3" >${account.key}</td>
												<c:set value="${account.value}" var="summaryBean"/>
											    <td class="text-right">
										            <div>
									                  <fmt:formatNumber value="${not empty summaryBean && not empty summaryBean['debitbean'] ? summaryBean['debitbean']['net_amount'] : '0.00'}" pattern="0.00"/>
								                    </div>
									            </td>
									            <td class="text-right">
								                    <div>
								                      <fmt:formatNumber value="${not empty summaryBean && not empty summaryBean['creditbean'] ? summaryBean['creditbean']['net_amount'] : '0.00'}" pattern="0.00"/>
						                            </div>
						                        </td>
											</tr>
										</c:forEach>
									</tbody>
								</table>
							</div>
							</c:forEach>
						</div>
					</div>
				</c:if>
				<%-- 
				<!-- Modal to change the zoho credentials, do we need it -->
					<div class="ui tiny modal credential-modal">
						<a class="close pad0 modal-close" href="/practo">Close</a>
							<div class="header">
								<h3 class="ui header">Modify Credentials</h3>
							</div>
							<div class="ui grid padded">
								<div class="ui row fluid icon input stackable padded">
									<label class="ui eight wide column">Authtoken</label>
									<div class="ui seven wide column">
										<input type="text" name="Authtoken">
									</div>
								</div>
								<div class="ui row fluid icon input stackable padded">
									<label class="ui eight wide column">Organization id</label>
									<div class="ui seven wide column">
										<input type="text" name="Organization id">
									</div>
								</div>
							</div>
							<div class="actions">
								<button type="button" class="ui button">Update</button>
							</div>
					</div>
				--%>	
				<!-- Summary section ends -->				
				<!-- Floating footer not required -->
				<c:if test="${hasResult}">
				<div class="sixteen wide column export-button-panel" style="width: 85% !important">
					<!--
					<a class="footer-link" href="/practo/logModal">Audit Log</a>
					<a class="footer-link" id='credential-modal'>Modify Credentials</a>
					-->
					<table class="fixed-panel">
						<tr class="export-button-container">
							<td class="fa-zero-width" width="25%">&nbsp;</td>
							<td width="976" colspan="2" class="export-button-container-table-cell">
								<table width="100%" cellspacing="0" cellpadding="0">
									<button type="submit" class="ui button basic f-right submit-export-button" id="zohoSync1"
										style="background-color: #ffa000 !important; color: white !important;"
										accesskey='e'
										><u>E</u>xport</button>
								</table>
							</td>
							<td class="fa-zero-width" width="25%">&nbsp;</td>
						</tr>
					</table>
					<!-- <button type="submit" class="ui button basic f-right" id="zohoSync">Export</button> -->
				</div>
				</c:if>
			</div>
			</form>
		</div>
		<!-- Main wrapper ends -->
	</div>
</body>
</html>
