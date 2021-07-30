<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<title><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.mgmt"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.stores.mgmt.toolbar");
		var userNameList = <%= request.getAttribute("userNameList") %>;
		var deptId = '${ifn:cleanJavaScript(dept_id)}';
		var gRoleId = '${roleId}';
		var accessstores = '${multiStoreAccess}';
		var gAccessAllow = '${genPrefs.recTransIndent}';
		var isUserHavingSuperStore = ${isUserHavingSuperStoreJson};
		var storeAccess = '${storeAccess}';
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/view_reject_indent.js"/>
	<insta:link type="script" file="stores/storescommon.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<style type="text/css">
		.scrolForContainer .yui-ac-content {
			 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    		_height:18em; max-width:35em; width:35em;/* ie6 */
		}
	</style>
	<script src="${cpath}/pages/stores/getItemMaster.do?ts=${master_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}"></script>
</head>
<c:set var="indentList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty indentList}"/>
<body onload="init(); showFilterActive(document.rejectIndentSearchForm);checkAllowAccess();">
<c:set var="indentno">
<insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.indentno"/>
</c:set>
<c:set var="item">
<insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.item"/>
</c:set>
<c:set var="rejectedbystore">
<insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.rejectedbystore"/>
</c:set>
<c:set var="raiseddate">
<insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.raiseddate"/>
</c:set>

<h1><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.rejectedtransferindentsdashboard"/></h1>

<insta:feedback-panel/>
<div id="storecheck" style="display: block;" >
<form name="rejectIndentSearchForm" method="GET">
	<input type="hidden" name="_method" value="listReject">
	<input type="hidden" name="_searchMethod" value="listReject"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<input type="hidden" name="_transaction" value="">
	<insta:search form="rejectIndentSearchForm" optionsId="optionalFilter" closed="${hasResults}" clearFunction="clearRejectedForm">
	  <div class="searchBasicOpts" >
		  <table class="searchFormTable">
			  <tr>
				<td>
					 <div class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.store"/></div>
						<div class="sboFieldInput">
						    <c:choose>
							    <c:when test="${roleId == 1 || roleId == 2  ||  (multiStoreAccess == 'A')}">
									<insta:userstores username="${userid}" elename="indent_store" id="indent_store" val="${param.indent_store }" onlySuperStores="Y"/>
								</c:when>
								<c:otherwise>
									<input type="hidden" name="indent_store" id="indent_store" value="${indent_store}" />
									<b>${dept_name}</b>
								</c:otherwise>
							</c:choose>
							<input type="hidden" name="indent_store@type" value="integer">
						</div>
				  	  </div>
				  	  </td>
				  	  <td>
				  	 	<div class="sboFieldLabel"><insta:ltext key="storemgmt.pharmacyindentlist.list.indentno"/></div>
						<div class="sboFieldInput">
							<input type="text" name="indent_no" id="indent_no" value="${ifn:cleanHtmlAttribute(param.indent_no)}"/>
							<input type="hidden" name="indent_no@type" value="integer" />
			    		</div>
			  		  </td>
			  		  <td>
			  		  	<div class="sfLabel"><insta:ltext key="storemgmt.pharmacyindentlist.list.itemname"/></div>
						<div class="sfField">
							<div id="itemWrapper">
								<input type="text" name="medicine_name" id="medicine_name" value="${ifn:cleanHtmlAttribute(param.medicine_name)}"/>
								<input type="hidden" name="medicine_name@op" value="co"/>
								<div id="item_dropdown" class="scrolForContainer"></div>
							</div>
						</div>
			  		  </td>
			  	</tr>
		  	</table>
		</div>

	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.receiveddate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.from"/>:</div>
							<insta:datewidget name="received_date" id="received_date0" value="${paramValues.received_date[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.to"/>:</div>
							<insta:datewidget name="received_date" id="received_date1" value="${paramValues.received_date[1]}"/>
							<input type="hidden" name="received_date@op" value="ge,le"/>
							<input type="hidden" name="received_date@type" value="date"/>
							<input type="hidden" name="received_date@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.raiseddate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.from"/>:</div>
							<insta:datewidget name="date_time" id="date_time0" value="${paramValues.date_time[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.to"/>:</div>
							<insta:datewidget name="date_time" id="date_time1" value="${paramValues.date_time[1]}"/>
							<input type="hidden" name="date_time@op" value="ge,le"/>
							<input type="hidden" name="date_time@type" value="date"/>
							<input type="hidden" name="date_time@cast" value="y"/>
						</div>
					</td>
				</tr>
		</table>
	  </div>
	</insta:search>
	<c:if test="${not empty pagedList.dtoList}">
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	</c:if>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<th><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.select"/></th>
				<insta:sortablecolumn name="indent_no" title="${indentno}"/>
				<insta:sortablecolumn name="medicine_name" title="${item}"/>
				<insta:sortablecolumn name="dept_from" title="${rejectedbystore}"/>
				<insta:sortablecolumn name="date_time" title="${raiseddate}"/>
				<th><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.batch.or.sl.no"/></th>
				<th><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.expirydate"/></th>
				<th><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.rejectedby"/></th>
				<th><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.rejectedqty"/></th>
			</tr>
			<c:forEach var="indent" items="${indentList}" varStatus="st">
			<c:set var="i" value="${st.index + 1}"/>
			<c:set var="view" value="${indent.status eq 'C' || indent.status eq 'T'}"/>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" onclick="showToolbar(${st.index}, event, 'resultTable',
						{indent_no:'${indent.indent_no }'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>
						<input type="checkbox" name="_select" id="_select${i}" onclick="setSelected(this);"/>
						<input type="hidden" name="_medicine_id" id="_medicine_id${i}" value="${indent.medicine_id}"/>
						<input type="hidden" name="_dept_id" id="_dept_id${i}" value="${indent.indent_store}"/>
						<input type="hidden" name="_batchno" id="_batchno${i}" value="${indent.batch_no}"/>
						<input type="hidden" name="_qty" id="_qty${i}" value="${indent.qty_rejected}"/>
						<input type="hidden" name="_indentno" id="_indentno${i}" value="${indent.indent_no}"/>
						<input type="hidden" name="_item_batch_no" id="_item_batch_no${i}" value="${indent.item_batch_id}"/>
						<input type="hidden" name="_transfer_detail_no" id="_transfer_detail_no${i}" value="${indent.transfer_detail_no}"/>
					</td>
					<td>${indent.indent_no}</td>
					<td>${indent.medicine_name}</td>
					<td>${indent.rej_store_name}</td>
					<td><fmt:formatDate value="${indent.date_time}" pattern="dd-MM-yyyy"/></td>
				    <td>${indent.batch_no}</td>
				    <td>${indent.exp_dt}</td>
				    <td>${indent.received_by}</td>
				    <td>${indent.qty_rejected}
				    <input type="hidden" name="_selected" id="_selected${i}" value="N"/></td>
				</tr>

			</c:forEach>
		</table>

		<c:if test="${param._method == 'listReject'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

    <c:if test="${not empty indentList}">
    	<div class="screenActions">
			<table>
				<tr>
					<td>
						<button type="button" class="button" style="width: 130px;" name="doactionButton" onclick="funActions('takeIntoStock')" accesskey="R"><b><u><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.r"/></u></b><insta:ltext key="storeprocurement.rejectedtransferedindentsdashboard.view.eturnintostock"/></button>
					</td>
					<td width="10%">&nbsp;</td>
				</tr>
			</table>
		</div>
	</c:if>

</form>
</div>
</body>
</html>