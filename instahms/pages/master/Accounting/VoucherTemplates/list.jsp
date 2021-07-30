<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Account Voucher Templates - Insta HMS</title>
		<insta:link type="css" file="widgets.css"/>
		<script type="text/javascript">
			var toolbar = {
				Edit: {
					title: "View/Edit",
					imageSrc: "icons/Edit.png",
					href: 'master/billingpreferences/accountingvouchertemplates.do?_method=show',
					onclick: null,
					description: "View and/or Edit A/C Voucher Template details"
					}
			};

			function init() {
				createToolbar(toolbar);
			}

		</script>

	</head>
	<body onload="init();">
		<h1>Accounting Voucher Templates</h1>

		<insta:feedback-panel/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('')">
				<tr onmouseover="hideToolBar()">
					<th>#</th>
					<th>Template Name</th>
					<th>Username</th>
					<th>Customized</th>
					<th>Reason for Customization</th>
				</tr>

				<c:forEach items="${acc_vc_templates}" var="template" varStatus="st">
					<c:choose>
						<c:when test="${template.map.voucher_type == 'CSRECEIPT'}">
							<c:set var="title" value="Consolidated Sponcer Receipt Voucher Template"/>
						</c:when >
						<c:when test="${template.map.voucher_type == 'DEPOSIT'}">
							<c:set var="title" value="Deposit Voucher Template"/>
						</c:when >
						<c:when test="${template.map.voucher_type == 'HOSPBILL'}">
							<c:set var="title" value="Hospital Bill Voucher Template"/>
						</c:when >
						<c:when test="${template.map.voucher_type == 'PAYMENTDUES'}">
							<c:set var="title" value="Payment Dues Voucher Template" />
						</c:when >
						<c:when test="${template.map.voucher_type == 'PHARMACYBILL'}">
							<c:set var="title" value="Pharmacy Bill Voucher Template"/>
						</c:when >
						<c:when test="${template.map.voucher_type == 'PP'}">
							<c:set var="title" value="Payment Voucher Template"/>
						</c:when >
						<c:when test="${template.map.voucher_type == 'PR'}">
							<c:set var="title" value="Payment Refund Voucher Template"/>
						</c:when >
						<c:when test="${template.map.voucher_type == 'PURCHASE'}">
							<c:set var="title" value="Invoice Voucher Template"/>
						</c:when >
						<c:when test="${template.map.voucher_type == 'RECEIPT'}">
							<c:set var="title" value="Receipt Voucher Template"/>
						</c:when >
						<c:when test="${template.map.voucher_type == 'SRWDEBITNOTE'}">
							<c:set var="title" value="Store Returns with Debit Note Template"/>
						</c:when >
						<c:when test="${template.map.voucher_type == 'CSISSUED'}">
							<c:set var="title" value="Stores Consignment Stock Issued Template"/>
						</c:when >
						<c:when test="${template.map.voucher_type == 'CSRETURNED'}">
							<c:set var="title" value="Stores Consignment Stock Returns Template"/>
						</c:when >
					</c:choose>
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
							{voucher_type : '${template.map.voucher_type}', title : '${title}',
								customized : '${not empty template.map.template_content?'true':'false'}'},'');" id="toolbarRow${st.index}">
							<td>${(pagedList.pageNumber-1)*pagedList.pageSize+st.index+1}</td>
							<td>
								${title}
							</td>
							<td>${template.map.username}</td>
							<td>${not empty template.map.template_content?'Yes':'No'}</td>
							<td><c:out value="${template.map.reason}"/></td>
						</tr>
				</c:forEach>

			</table>
		</div>

	</body>
</html>
