<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<html>
<head>
	<title><insta:ltext key="storemgmt.processindentlist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.mgmt"/>
	<insta:js-bundle prefix="stores.mgmt.indents"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.stores.mgmt.toolbar");
		var userNameList = <%= request.getAttribute("userNameList") %>;
		var getReport = '${ifn:cleanJavaScript(report)}';
		var indentno = '${indent_no}';
		var deptId = '${pharmacyStoreId}';
 		var gRoleId = '${roleId}';
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/view_process_indent.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>

	<style type="text/css">
		.scrolForContainer .yui-ac-content {
			 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    		_height:18em; max-width:35em; width:35em;/* ie6 */
		}
	</style>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="indentProcessList" value="${not empty pagedList.dtoList ? pagedList.dtoList : null}"/>
<c:set var="hasResults" value="${not empty indentProcessList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="O" value="Open"/>
<c:set target="${statusDisplay}" property="A" value="Approved"/>
<c:set target="${statusDisplay}" property="R" value="Rejected"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>
<c:set target="${statusDisplay}" property="P" value="Processed"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<jsp:useBean id="indentDisplay" class="java.util.HashMap"/>
<c:set target="${indentDisplay}" property="S" value="Stock Transfer"/>
<c:set target="${indentDisplay}" property="U" value="Dept / Ward Issue"/>
<c:set var="allowCrossCenterIndents" value = "<%=GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents()%>" />
<body onload="init(); showFilterActive(document.indentProcessSearchForm);generateReport();">
<c:set var="all">
<insta:ltext key="storemgmt.processindentlist.list.all"/>
</c:set>
<c:set var="department">
<insta:ltext key="storemgmt.processindentlist.list.department"/>
</c:set>
<c:set var="indentno">
<insta:ltext key="storemgmt.processindentlist.list.indentno"/>
</c:set>
<c:set var="indenttype">
<insta:ltext key="storemgmt.processindentlist.list.indenttype"/>
</c:set>
<c:set var="raisedby">
<insta:ltext key="storemgmt.processindentlist.list.raisedby"/>
</c:set>
<c:set var="raiseddate">
<insta:ltext key="storemgmt.processindentlist.list.raiseddate"/>
</c:set>
<c:set var="expecteddate">
<insta:ltext key="storemgmt.processindentlist.list.expecteddate"/>
</c:set>
<c:set var="approveddate">
<insta:ltext key="storemgmt.processindentlist.list.approveddate"/>
</c:set>
<c:set var="dummyvalue">
<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="returndate">
<insta:ltext key="salesissues.stockuserreturnlist.list.returndate"/>
</c:set>
<c:set var="status">
<insta:ltext key="storemgmt.processindentlist.list.open"/>,
<insta:ltext key="storemgmt.processindentlist.list.approved"/>,
<insta:ltext key="storemgmt.processindentlist.list.rejected"/>,
<insta:ltext key="storemgmt.processindentlist.list.cancelled"/>,
<insta:ltext key="storemgmt.processindentlist.list.processed"/>,
<insta:ltext key="storemgmt.processindentlist.list.closed"/>
</c:set>
<c:set var="indentstatus">
<insta:ltext key="storemgmt.processindentlist.list.stocktransfer"/>,
<insta:ltext key="storemgmt.processindentlist.list.dept.wardissue"/>
</c:set>
<div id="storecheck" style="display: block;" >
	<h1><insta:ltext key="storemgmt.processindentlist.list.indentprocessdashboard"/></h1>
	<insta:feedback-panel/>
	<div>
		<c:choose>
			<c:when test="${param._transfermessage != null && param._transfermessage != ''}" >
			<span class="resultMessage"><insta:ltext key="storemgmt.processindentlist.list.transferno"/>
			<a href="${pageContext.request.contextPath}/pages/stores/stocktransfer.do?_method=getStockTransferPrint&transfer_no=${ifn:cleanURL(param._transfermessage)}"
				target="_blank"> ${ifn:cleanHtml(param._transfermessage)} </a> <insta:ltext key="storemgmt.processindentlist.list.generated.transferindentno"/> ${ifn:cleanHtml(param._indentno)}</span>
			</c:when>
			<c:when test="${param._issuemessage != null && param._issuemessage != ''}" >
				<span class="resultMessage"><insta:ltext key="storemgmt.processindentlist.list.issueno"/>
				<a href="${pageContext.request.contextPath}/DirectReport.do?report=StoreStockUserIssues&issNo=${ifn:cleanURL(param._issuemessage)}"
				target="_blank"> ${ifn:cleanHtml(param._issuemessage)} </a> <insta:ltext key="storemgmt.processindentlist.list.generated.dept.ward.issueno"/> ${ifn:cleanHtml(param._indentno)}</span>
			</c:when>
			<c:otherwise>
				<span class="resultMessage">${ifn:cleanHtml(param._success) }</span>
			</c:otherwise>
		</c:choose>
	</div>
	<br/>
</div>
<form name="indentProcessSearchForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="indentProcessSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts">
		  	<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="storemgmt.processindentlist.list.raisedbyuser.store"/></div>
					<div class="sboFieldInput" style="width: 15em;">
						<div id="userName_wrapper" style="width: 15em;">
							<input type="text" name="requester_name" id="requester_name" value="${ifn:cleanHtmlAttribute(param.requester_name)}" style="width: 130px;"/>
						<div id="userName_dropdown"></div>
					</div>
		    	</div>
		  	</div>
		 	<div class="sboField">
		 		<div class="sboFieldLabel"><insta:ltext key="storemgmt.processindentlist.list.requestingstore"/></div>
			 	<div class="sboFieldInput">
			 		<c:choose>
			 			<c:when test="${allowCrossCenterIndents == 'Y'}">
			 				<insta:selectdb  name="dept_from" value="${param.dept_from}"
							table="stores" valuecol="dept_id" dummyvalue="${all}" displaycol="dept_name" orderby="dept_name" class="dropdown"/>
							<input type="hidden" name="dept_from@type" value="string"/>
							<input type="hidden" name="dept_from@cast" value="y"/>
			 			</c:when>
			 			<c:otherwise>
			 				<select name="dept_from" id="dept_from" class = "dropdown">
			 					<option value="" selected="selected">${all}</option>
								<c:forEach var="stores" items="${storesList}">
										<option value="${stores.map.dept_id}" ${param.dept_from == stores.map.dept_id ? 'selected':''}>${stores.map.dept_name}</option>
								</c:forEach>
							</select>
							<input type="hidden" name="dept_from@type" value="string"/>
							<input type="hidden" name="dept_from@cast" value="y"/>
			 			</c:otherwise>
			 		</c:choose>
				</div>
			</div>
			<c:choose>
			<c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
			 	<div class="sboField">
				 	<div class="sboFieldLabel"><insta:ltext key="storemgmt.processindentlist.list.indentstore"/></div>
				 	<div class="sboFieldInput">
						<insta:userstores username="${userid}" elename="indent_store" id="indent_store" onlySuperStores="Y" val="${param.indent_store}" defaultValue="${all}"/>
					</div>
				</div>
			</c:when>
			<c:otherwise>
				<div class="sboField">
				 	<div class="sboFieldLabel"><insta:ltext key="storemgmt.processindentlist.list.indentstore"/></div>
				 	<div class="sboFieldInput">
						<b><insta:getStoreName store_id="${pharmacyStoreId}"/></b>
					</div>
				 	<input type="hidden" name="indent_store" id="indent_store" value="${pharmacyStoreId}">
				 </div>
			</c:otherwise>
			</c:choose>
			<input type="hidden" name="indent_store@type" value="integer"/>
			<input type="hidden" name="indent_store@cast" value="y"/>
			<div class="sboField">
		 		<div class="sboFieldLabel"><insta:ltext key="storemgmt.processindentlist.list.dept"/></div>
			 	<div class="sboFieldInput">
				 	<insta:selectdb  name="dept" value="${param.dept}" table="department" valuecol="dept_id" displaycol="dept_name" dummyvalue="${department}"/>
				</div>
			</div>
			<div class="sboField">
		 		<div class="sboFieldLabel"><insta:ltext key="storemgmt.processindentlist.list.ward"/></div>
			 	<div class="sboFieldInput">
			 		<select name="ward" id="ward" class="dropdown" >
						<option value=""><insta:ltext key="storemgmt.processindentlist.list.wardoption"/></option>
						<c:forEach items="${wards }" var="ward">
							<option value="${ward.map.ward_no }" >
								${ward.map.ward_name }
							</option>
						</c:forEach>
					</select>
				</div>
			</div>
			<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storemgmt.processindentlist.list.indentno"/>.</div>
				<div class="sboFieldInput">
					<input type="text" name="indent_no" id="indent_no" value="${ifn:cleanHtmlAttribute(param.indent_no)}" onkeypress="return enterNumOnlyzeroToNine(event);"/>
					<input type="hidden" name="indent_no@type" value="integer"/>
				</div>
	  		</div>
		</div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  <table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel"><insta:ltext key="storemgmt.processindentlist.list.expecteddate"/></div>
				<div class="sfField">
					<div class="sfFieldSub"><insta:ltext key="storemgmt.processindentlist.list.from"/>:</div>
					<insta:datewidget name="expected_date" id="expected_date0" value="${paramValues.expected_date[0]}"/>
					</div>
				<div class="sfField">
					<div class="sfFieldSub"><insta:ltext key="storemgmt.processindentlist.list.to"/>:</div>
					<insta:datewidget name="expected_date" id="expected_date1" value="${paramValues.expected_date[1]}"/>
					<input type="hidden" name="expected_date@op" value="ge,le"/>
					<input type="hidden" name="expected_date@cast" value="y"/>
				</div>
			</td>
			<td>
				<div class="sfLabel"><insta:ltext key="storemgmt.processindentlist.list.raiseddate"/></div>
				<div class="sfField">
					<div class="sfFieldSub"><insta:ltext key="storemgmt.processindentlist.list.from"/>:</div>
					<insta:datewidget name="date_time" id="date_time0" value="${paramValues.date_time[0]}"/>
					</div>
				<div class="sfField">
					<div class="sfFieldSub"><insta:ltext key="storemgmt.processindentlist.list.to"/>:</div>
					<insta:datewidget name="date_time" id="date_time1" value="${paramValues.date_time[1]}"/>
					<input type="hidden" name="date_time@op" value="ge,le"/>
					<input type="hidden" name="date_time@cast" value="y"/>
				</div>
			</td>
			<td>
				<div class="sfLabel"><insta:ltext key="storemgmt.processindentlist.list.status"/></div>
				<div class="sfField">
					<insta:checkgroup name="status" selValues="${paramValues.status}" opvalues="O,A,R,X,P,C" optexts="${status}"/>
				</div>
			</td>
			<td>
				<div class="sfLabel"><insta:ltext key="storemgmt.processindentlist.list.indenttype"/></div>
				<div class="sfField">
					<insta:checkgroup name="indent_type" optexts="${indentstatus}" opvalues="S,U" selValues="${paramValues.indent_type}"/>
				</div>
			</td>

			<td>
				<div class="sfLabel"><insta:ltext key="storemgmt.processindentlist.list.itemname"/></div>
				<div class="sfField">
					<div id="itemWrapper">
						<input type="text" name="item_name" id="item_name" value="${ifn:cleanHtmlAttribute(param.item_name)}"/>
						<div id="item_dropdown" class="scrolForContainer"></div>
					</div>
				</div>
			</td>

		</tr>
		<c:if test="${multiCentered && centerId == 0}">
		<tr>
			<td>
				<div class="sfLabel"><insta:ltext key="storemgmt.processindentlist.list.requestingcenter"/></div>
				<div class="sfField">
					<select class="dropdown" name="requesting_center_id" id="requesting_center_id">
						<option value="">${dummyvalue}</option>
						<c:forEach items="${centers}" var="center">
							<option value="${center.map.center_id}"
								${param.requesting_center_id == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
						</c:forEach>
					</select>
					<input type="hidden" name="requesting_center_id@cast" value="y"/>
				</div>
			</td>
		</tr>
		</c:if>
	  </table>
	  </div>
	</insta:search>
	<c:if test="${not empty pagedList.dtoList}">
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	</c:if>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<insta:sortablecolumn name="indent_no" title="${indentno}"/>
				<insta:sortablecolumn name="indent_type" title="${indenttype}"/>
				<c:if test="${multiCentered && centerId == 0}">
					<th>Requesting Center</th>
				</c:if>
				<insta:sortablecolumn name="requester_name" title="${raisedby}"/>
				<insta:sortablecolumn name="date_time" title="${raiseddate}"/>
				<insta:sortablecolumn name="expected_date" title="${expecteddate}"/>
				<insta:sortablecolumn name="approved_time" title="${approveddate}"/>
				<th><insta:ltext key="storemgmt.processindentlist.list.status"/></th>
				<th><insta:ltext key="storemgmt.processindentlist.list.approvedby"/></th>
				<th><insta:ltext key="storemgmt.processindentlist.list.requestedbystore.dept.ward"/></th>
			</tr>
			<c:if test="${not empty pagedList.dtoList}">
			<c:forEach var="indent" items="${indentProcessList}" varStatus="st">
			<c:set var="view" value="${indent.status ne 'A'}"/>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{indent_no:'${indent.indent_no }'},
						[${view },!${view},true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${indent.indent_no}</td>
					<td>${indentDisplay[indent.indent_type]}</td>
					<c:if test="${multiCentered && centerId == 0}">
						<td><insta:getCenterName center_id="${indent.requesting_center_id}"/></td>
					</c:if>
					<td>${indent.requester_name}</td>
					<td><fmt:formatDate value="${indent.date_time }" pattern="dd-MM-yyyy"/></td>
					<td><fmt:formatDate value="${indent.expected_date }" pattern="dd-MM-yyyy"/></td>
					<td><fmt:formatDate value="${indent.approved_time }" pattern="dd-MM-yyyy"/></td>
				    <td>${statusDisplay[indent.status]}</td>
					<td>${indent.approved_by }</td>
					<td>${indent.dept_from_name }</td>
				</tr>
			</c:forEach>
			</c:if>
		</table>

		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
    </div>
</form>
<script>
 	var gRoleId = '${roleId}';
	</script>
</body>
</html>