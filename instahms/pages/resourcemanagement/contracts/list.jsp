<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Contracts List - Insta HMS</title>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		function init() {
			createToolbar(toolbar);
		}
		var toolbar = {
			Edit: {
				title: 'View/Edit',
				imageSrc: 'icons/Edit.png',
				href: '/resourcemanagement/contracts.do?method=show'
			}
		}
		function doSearch() {
			var mrNo = document.getElementById("status").value;
			if(empty(mrNo)) {
				showMessage ("js.patient.diag.status.entermrnotosearch");
				document.getElementById("mrno").focus();
				return false;
			}	
			return true;
		}
	</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<body onload="init(); showFilterActive(document.validateSearch);">
<c:set var="filterclosed" value="${not empty pagedList.dtoList}"></c:set>

<form name="validateSearch">
	<input type="hidden" name="method" value="list">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list">
	<h1>Contracts</h1>
	<insta:feedback-panel/>

	<insta:search form="validateSearch" optionsId="optionalFilter" closed="${filterclosed}">
		<div class="searchBasicOpts" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sboField">
							<div class="sboFieldLabel">Status:</div>
								<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
						</div>
					</td>
					<td class="last">
						<div class="sboField">
							<div class="sboFieldLabel">Contract Type:</div>
							<select name="contract_type_id" id="contract_type_id" class="dropdown">
								<option value=""  "${contract_type_id == 0 ?'selected' : ''}">...Select...</option>
								<c:forEach var="item" items="${requestScope.contractTypes}">
									<c:set var="selected" value=""/>
									<c:if test= "${item.CONTRACT_TYPE_ID == contract_type_id}">
										<c:set var="selected" value="selected"/>
									</c:if>
									<option value="${item.CONTRACT_TYPE_ID}" ${selected}>${item.CONTRACT_TYPE} </option>
								</c:forEach>
							</select>
						</div>
					</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
				</tr>
			</table>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${filterclosed ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Renewal Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="renewalFrom" id="renewalFrom" />
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="renewalTo" id="renewalTo" />
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">End Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="expiryFrom" id="expiryFrom" />
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="expiryTo" id="expiryTo" />
						</div>
					</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
				</tr>
			</table>
		</div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

<table class="formtable" width="100%">

	<tr>
		<td>
			<table class="dashboard" align="center" width="100%" id="recordsTable">
				<tr>
					<th>Contracts</th>
					<th>Contract Type</th>
					<th>Start Date</th>
					<th>End Date</th>
					<th>Renewal Date</th>
					<th>Value</th>
					<th>Status</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'recordsTable',
							{contract_id: '${record.map['contract_id']}'});"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<c:choose>
						<c:when test="${record.map['contract_status'] eq 'A'}">
							<c:set var="flagColor" value="green"/>
						</c:when>
						<c:when test="${record.map['contract_status'] eq 'I'}">
							<c:set var="flagColor" value="red"/>
						</c:when>
					</c:choose>
					<td><img src="${cpath}/images/${flagColor}_flag.gif"/>${record.map['contract_company']}</td>
					<td>${record.map['contract_type']}</td>
					<td><fmt:formatDate value="${record.map['contract_start_date']}" pattern="dd-MM-yyyy"/></td>
					<td><fmt:formatDate value="${record.map['contract_end_date']}" pattern="dd-MM-yyyy"/></td>
					<td><fmt:formatDate value="${record.map['contract_renewal_date']}" pattern="dd-MM-yyyy"/></td>
					<td>${record.map['contract_value']}</td>
					<td>
						<c:if test="${record.map['contract_status'] eq 'I'}"><font class="${color}" >Inactive</font></c:if>
						<c:if test="${record.map['contract_status'] eq 'A'}">Active</c:if>
					</td>
				</tr>
				</c:forEach>
			</table>
		</td>
	</tr>
	</table>

	<insta:noresults hasResults="${filterclosed}"/>
	<div class="screenActions">
		<c:url var="addnew" value="contracts.do">
			<c:param name="method" value="add"></c:param>
		</c:url>
		<a href="${addnew}">Add New Contract</a>
	</div>

	<table align="right">
		<tr>
			<td><img class="flag" src="${cpath}/images/green_flag.gif"/></td>
			<td>Active&nbsp;</td>
			<td><img class="flag" src="${cpath}/images/red_flag.gif"/></td>
			<td>InActive</td>
		</tr>
	</table>
</form>
</body>
</html>
