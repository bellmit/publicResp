<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:set var="typeOfSale" value=""/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
	<title><insta:ltext key="storemgmt.storesstockledger.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="stores/stockledger.js"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="ajax.js" />

	<style type="text/css">
	 	.scrolForContainer .yui-ac-content{
		 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
	    _height:18em; max-width:35em; width:35em;/* ie6 */
		}
 	</style>

<c:if test="${not empty pharmacyStoreId}">
	<script src="${cpath}/pages/stores/getMedicinesInStock.do?ts=${medicine_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}&includeZeroStock=Y&includeUnapproved=Y&includeConsignmentStock=Y&storeId=${pharmacyStoreId}"></script>
</c:if>

<script>
var selBatch = '${ifn:cleanJavaScript(param.batchno)}';
var medicine_Name = '${param.medicineName}';
var store_id = '${param.store_id}';
var fromDateTime = '${param.fromDateTime}';
var toDateTime = '${param.toDateTime}';
var batchno = '${param.batchno}';
var period = '${param.period}';
var fromDate = '${param.fromDate}';
var toDate = '${param.toDate}';
var fromCheckPt = '${param.fromCheckPt}';
var toCheckPt = '${param.toCheckPt}';
</script>
<insta:js-bundle prefix="stores.mgmt"/>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="ledgerList" value="${pagedList.dtoList}"/>
<c:set var="method_name" value= "getScreen"/>
<body onload="init();checkstoreallocation();" class="yui-skin-sam">
<c:set var="txndate">
<insta:ltext key="storemgmt.storesstockledger.list.txndate"/>
</c:set>
<c:set var="batch">
<insta:ltext key="storemgmt.storesstockledger.list.batch"/>
</c:set>
<c:set var="user">
<insta:ltext key="storemgmt.storesstockledger.list.user"/>
</c:set>
<c:set var="txntype">
<insta:ltext key="storemgmt.storesstockledger.list.txntype"/>
</c:set>
<c:set var="stocktransferText">
<insta:ltext key="storemgmt.storesstockledger.list.stocktransfer"/>
</c:set>
<c:set var="select">
<insta:ltext key="storemgmt.storesstockledger.list.select"/>
</c:set>
<c:set var="searchText">
<insta:ltext key="storemgmt.storesstockledger.list.searchText"/>
</c:set>
<c:set var="clearText">
<insta:ltext key="storemgmt.storesstockledger.list.clearText"/>
</c:set>

	<c:choose><c:when test="${not empty param.title}">
		<div class="pageHeader">${ifn:cleanHtml(param.title)}</div>
	</c:when><c:otherwise>
		<div class="pageHeader"><insta:ltext key="storemgmt.storesstockledger.list.storesstockledger"/></div>
	</c:otherwise></c:choose>

<div id="storecheck" style="display: block;" >
	<form name="stockLedger" method="GET">
		<input type="hidden" name="_method" value="list">
		<input type="hidden" name="medicine" id="medicine" value="">
		<input type="hidden" name="fromDateTime" />
		<input type="hidden" name="toDateTime" />
		<input type="hidden" name="storeName" value="${ifn:cleanHtmlAttribute(param.storeName)}"/>
		<input type="hidden" name="export_type" id="export_type" value=""/>
		
		<div class="stwMain">
			<div class="stwHeader ${not empty ledgerList ? 'stwClosed' : ''}" id="filter" onclick="stwToggle(this);">
				<label><insta:ltext key="storemgmt.storesstockledger.list.filter"/></label>
			</div>
			<div id="filter_content" class="stwContent ${not empty ledgerList ? 'stwHidden' : ''}">
				<table align="center" class="search" width="80%">
					<tr>
						<td style="width:5em"><insta:ltext key="storemgmt.storesstockledger.list.store"/>:</td>
						<c:choose>
						<c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
						<td style="width:15em">
						<insta:userstores username="${userid}" elename="store_id" id="store_id" onchange="onChangeStore();" val="${param.store_id}"/>
						</td>

						</c:when>
						<c:otherwise>
						<td style="width:15em"><b><insta:getStoreName store_id="${pharmacyStoreId}"/></b></td>
						<input type="hidden" name="store_id" id="store_id" value="${pharmacyStoreId}"/>

						</c:otherwise>
						</c:choose>
						<td valign="top"><insta:ltext key="storemgmt.storesstockledger.list.item"/> :
							<div id="medicineName_wrapper" style="width: 15em; padding-bottom:1em;position: absolute;display:inline;  ">
				     			<input type="text" name="medicineName" id="medicineName" style="width: 15em"
				     			maxlength="100" value="${ifn:cleanHtmlAttribute(param.medicineName)}"/>
				     			<span class="star" style="padding-left:180px;">*</span>
				     			<div id="medicineName_dropdown" class="scrolForContainer"></div>
				     		</div>
						</td>
						<td valign="top"><insta:ltext key="storemgmt.storesstockledger.list.batch"/>:
							<div id="batch_wrapper" style="width: 15em; padding-bottom:1em;position: absolute;display:inline;  ">
						     <input type="text" name="batchno" id="batchno" style="width: 12em" maxlength="50" value="${ifn:cleanHtmlAttribute(param.batchno)}"/>

						    <div id="batch_dropdown"></div></div>
						</td>
					</tr>
				</table>
				<table align="center" class="search" width="60%">
					<tr>
						<td><insta:ltext key="storemgmt.storesstockledger.list.period"/>:</td>
						<td>
							<table>
								<tr>
									<td colspan="2">
										<input type="radio" id="history" name="period" value="history" checked
											onchange="onChecked('history');" >
										<label for="history"><insta:ltext key="storemgmt.storesstockledger.list.fullhistory"/></label>
									</td>
								</tr>
								<tr>
									<td colspan="2">
										<input type="radio" id="dates" name="period" value="dates"
											<c:if test="${param.period eq 'dates' }"><insta:ltext key="storemgmt.storesstockledger.list.checked"/></c:if> onchange="onChecked('dates');" >
										<label for="dates"><insta:ltext key="storemgmt.storesstockledger.list.daterange"/></label>
									</td>
									<td><insta:ltext key="storemgmt.storesstockledger.list.from"/>:</td>
									<td><insta:datewidget name="fromDate" valid="past" value="${param.fromDate}"/></td>
									<td><insta:ltext key="storemgmt.storesstockledger.list.to"/>:</td>
									<td><insta:datewidget name="toDate" valid="past" value="${param.toDate}"/></td>
								</tr>
								<tr>
									<td colspan="2">
										<input type="radio" id="checkpoints" name="period"	value="checkpoints"
											<c:if test="${param.period eq 'checkpoints' }"><insta:ltext key="storemgmt.storesstockledger.list.checked"/></c:if>	onchange="onChecked('checkpoints');" >
										<label for="checkpoints"><insta:ltext key="storemgmt.storesstockledger.list.checkpoints"/></label>
									</td>
									<td><insta:ltext key="storemgmt.storesstockledger.list.from"/>:</td>
									<td>
									<select name="fromCheckPt" onchange="setFromDate" class="dropdown">
					     				<option selected value="">${select}</option>
					     				<c:forEach var="checkPt" items="${checkPoints}">
					     					<option value="${checkPt.checkpoint_id}"
					     					 <c:if test="${checkPt.checkpoint_id eq param.fromCheckPt}"> <insta:ltext key="storemgmt.storesstockledger.list.selected"/> </c:if> >${checkPt.checkpoint_name}</option>
					     				</c:forEach>
					 				 </select>
									</td>

									<td><insta:ltext key="storemgmt.storesstockledger.list.to"/>:</td>
									<td>
									<select name="toCheckPt" class="dropdown">
					     				<option selected value="">${select}</option>
					     				<option value="-1" <c:if test="${-1 eq param.toCheckPt}"> <insta:ltext key="storemgmt.storesstockledger.list.selected"/> </c:if> ><insta:ltext key="storemgmt.storesstockledger.list.nowoption"/></option>
					     				<c:forEach var="checkPt" items="${checkPoints}">
					     					<option value="${checkPt.checkpoint_id}"
					     					<c:if test="${checkPt.checkpoint_id eq param.toCheckPt}"> <insta:ltext key="storemgmt.storesstockledger.list.selected"/> </c:if> >${checkPt.checkpoint_name}</option>
					     				</c:forEach>
					 				 </select>
									</td>
								</tr>
							</table>
						</td>
					</tr>

					<tr>
						<td colspan="7" align="right">
							<input type="submit" value="${searchText}" onclick="return doSearch();" >
							<input type="button" value="${clearText}" onclick="clearSearch();">
						</td>
					</tr>

				</table>
			</div>
		</div>
		<c:choose>

			<c:when test="${empty ledgerList and empty openStockList}">
				<p><insta:ltext key="storemgmt.storesstockledger.list.noresultfound"/> </p>
			</c:when>
			<c:otherwise>
				<insta:ltext key="storemgmt.storesstockledger.list.stockmovement.store"/>:<b> ${ifn:cleanHtml(param.storeName)}</b>, <insta:ltext key="storemgmt.storesstockledger.list.item"/>: <b>${ifn:cleanHtml(param.medicineName)}</b>
				<table class="detailList" cellspacing="0" cellpadding="0" align="center" width="100%">
					<tr>
						<insta:sortablecolumn name="txn_date" title="${txndate}"/>
						<insta:sortablecolumn name="batch_no" title="${batch}"/>
						<insta:sortablecolumn name="user_name" title="${user}"/>
						<insta:sortablecolumn name="txn_type" title="${txntype}"/>
						<th><insta:ltext key="storemgmt.storesstockledger.list.txnref"/>#</th>
						<th><insta:ltext key="storemgmt.storesstockledger.list.details"/></th>

						<c:if test="${roleId eq '1' or roleId eq '2' or actionRightsMap.view_all_rates eq 'A'}">
							<th class="number"><insta:ltext key="storemgmt.storesstockledger.list.mrp.pkg.in.brackets"/></th>
						</c:if>

						<th class="number"><insta:ltext key="storemgmt.storesstockledger.list.bonusqty"/></th>
						<th class="number"><insta:ltext key="storemgmt.storesstockledger.list.totalqty"/></th>
					</tr>

					<c:set var="ledgerURL" value="StockLedgerAction.do?method=get"/>

					<c:forEach  var="open" items="${openStockList}">
						<tr>
							<td>--</td>
							<td>${open.batch_no}</td>
							<td>--</td>
							<td><insta:ltext key="storemgmt.storesstockledger.list.openingstock"/></td>
							<td>--</td>
							<td>--</td>

							<c:if test="${roleId eq '1' or roleId eq '2' or actionRightsMap.view_all_rates eq 'A'}">
							<td class="number">${open.mrp}</td>
							</c:if>

							<td></td>
							<td class="number">${open.qty}</td>
						</tr>
					</c:forEach>

					<c:forEach var="ledger" items="${ledgerList}" varStatus="st">
					<c:set var="txnUrl" >
						<c:choose>
							<c:when test="${ledger.txn_type eq 'Purchase'}">${cpath}/stores/stockentry.do?_method=generateGRNprint&grNo=${ledger.txn_ref}</c:when>
							<c:when test="${ledger.txn_type eq 'Sales' or ledger.txn_type eq 'Returns'}">${cpath}/pages/bill/MedicineSalesPrint.do?_method=getSalesPrint&saleId=${ledger.txn_ref}&printerId=0&duplicate=true</c:when>
							<c:when test="${ledger.txn_type eq 'Debit'}">${cpath}/stores/StoresSupplierReturnslist.do?_method=generatePrintForItemReturnNote&debitNo=${ledger.txn_ref}</c:when>
							<c:when test="${ledger.txn_type eq 'Supplier Returns'}">${cpath}/stores/StoresSupplierReturnslist.do?_method=generatePrintForItemReturnNote&return_no=${ledger.txn_ref}</c:when>
							<c:when test="${ledger.txn_type eq 'Replace'}">${cpath}/stores/StoresSupplierReturnslist.do?_method=generatePrintForItemReturnNote&return_no=${ledger.txn_ref}</c:when>
							<c:when test="${ledger.txn_type eq 'Adjustment'}">${cpath}/DirectReport.do?report=PharmacyStockAdjustmentReport&adjNo=${ledger.txn_ref}</c:when>
							<c:when test="${ledger.txn_type eq 'Stock Transfer'}">${cpath}/pages/stores/stocktransfer.do?_method=getStockTransferPrint&transfer_no=${ledger.txn_ref}</c:when>
							<c:when test="${ledger.txn_type eq 'Patient Issues'}">${cpath}/DirectReport.do?report=StoreStockUserIssues&issNo=${ledger.txn_ref}</c:when>
							<c:when test="${ledger.txn_type eq 'Patient Returns'}">${cpath}/DirectReport.do?report=StoreStockUserReturns&returnNo=${ledger.txn_ref}</c:when>
							<c:when test="${ledger.txn_type eq 'User Issues'}">${cpath}/DirectReport.do?report=StoreStockUserIssues&issNo=${ledger.txn_ref}</c:when>
							<c:when test="${ledger.txn_type eq 'User Returns'}">${cpath}/DirectReport.do?report=StoreStockUserReturns&returnNo=${ledger.txn_ref}</c:when>
						</c:choose>
					</c:set>
						<tr >
							<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm:ss" value="${ledger.txn_date }" /></td>
							<td>${ledger.batch_no}</td>
							<td>${ledger.user_name}</td>
							<td>${ledger.txn_type}</td>
							<td><a href="${txnUrl}" target="_blank" >${ledger.txn_ref}</a></td>
							<td>${ledger.details }</td>

							<c:if test="${roleId eq '1' or roleId eq '2' or actionRightsMap.view_all_rates eq 'A'}">
							<td class="number">${ledger.mrp}</td>
							</c:if>

							<td class="number">${ledger.bonus_qty}</td>
							<td class="number">${ledger.total_qty}</td>
					</tr>
				</c:forEach>

				<c:forEach  var="closing" items="${closingStock}">
						<tr>
							<td>--</td>
							<td>${closing.batch_no}</td>
							<td>--</td>
							<td><insta:ltext key="storemgmt.storesstockledger.list.closingstock"/></td>
							<td>--</td>
							<td>--</td>

							<c:if test="${roleId eq '1' or roleId eq '2' or actionRightsMap.view_all_rates eq 'A'}">
							<td class="number">${closing.mrp}</td>
							</c:if>

							<td class="number"></td>
							<td class="number">${closing.qty}</td>
						</tr>
					</c:forEach>
					
					<c:forEach  var="transit" items="${transitStock}">
						<tr>
							<td>--</td>
							<td>${transit.batch_no}</td>
							<td>--</td>
							<td><insta:ltext key="storemgmt.storesstockledger.list.transitqty"/></td>
							<td>--</td>
							<td>--</td>
							
							<c:if test="${roleId eq '1' or roleId eq '2' or actionRightsMap.view_all_rates eq 'A'}">
							<td class="number">${transit.mrp}</td>
							</c:if>
							<td class="number"></td>
							<td class="number">${transit.transit_qty}</td>
						</tr>
					</c:forEach>
					
					<c:forEach  var="rejected" items="${rejectedStock}">
						<tr>
							<td>--</td>
							<td>${rejected.batch_no}</td>
							<td>--</td>
							<td><insta:ltext key="storemgmt.storesstockledger.list.rejectedqty"/></td>
							<td>--</td>
							<td>--</td>
							
							<c:if test="${roleId eq '1' or roleId eq '2' or actionRightsMap.view_all_rates eq 'A'}">
							<td class="number">${rejected.mrp}</td>
							</c:if>
							<td class="number"></td>
							<td class="number">${rejected.qty_in_rejection}</td>
						</tr>
					</c:forEach>
			</table>

			<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
			<input type="button" value="Export To Csv" onclick="exportData('CSV')">
			<!-- <input type="button" value="Export To Excel" onclick="exportData('EXCEL')"> -->    
		</c:otherwise>
	</c:choose>

	</form>
	</div>
<script>
		var checkPoints = ${checkpointJSON};
		var deptId = '${ifn:cleanJavaScript(dept_id)}';
		var gRoleId = '${roleId}';
		var sesHospitalId = '${ifn:cleanJavaScript(sesHospitalId)}';
</script>


</body>
</html>

