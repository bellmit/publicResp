<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO,com.insta.hms.common.Encoder" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="indentList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty indentList}"/>

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

<c:set var="allowDecimalsForQty" value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowdecimalsforqty()%>" />

<html>
<head>
	<title><insta:ltext key="storemgmt.pharmacyindentlist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.mgmt"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.stores.mgmt.toolbar");
		var userNameList = <%= request.getAttribute("userNameList") %>;
		var userName = '<%=Encoder.cleanJavaScript((String)request.getAttribute("username"))%>';
		var getReport = '${ifn:cleanJavaScript(report)}';
		var indentno = '${indentno}';
		var allowDecimalsForQty = '${allowDecimalsForQty}';
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/view_indent.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>

	<style type="text/css">
		.scrolForContainer .yui-ac-content {
			 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    		_height:18em; max-width:35em; width:35em;/* ie6 */
		}
	</style>
	<script src="${cpath}/pages/stores/getItemMaster.do?ts=${master_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}"></script>
</head>
<body onload="init(); showFilterActive(document.indentSearchForm); generateReport();">
<c:set var="status">
<insta:ltext key="storemgmt.pharmacyindentlist.list.open"/>,
<insta:ltext key="storemgmt.pharmacyindentlist.list.approved"/>,
<insta:ltext key="storemgmt.pharmacyindentlist.list.rejected"/>,
<insta:ltext key="storemgmt.pharmacyindentlist.list.cancelled"/>,
<insta:ltext key="storemgmt.pharmacyindentlist.list.processed"/>,
<insta:ltext key="storemgmt.pharmacyindentlist.list.closed"/>
</c:set>
<c:set var="indenttype">
<insta:ltext key="storemgmt.pharmacyindentlist.list.stocktransfer"/>,
<insta:ltext key="storemgmt.pharmacyindentlist.list.deptorwardissue"/>
</c:set>
<c:set var="indentno">
<insta:ltext key="storemgmt.pharmacyindentlist.list.indentno"/>
</c:set>
<c:set var="indenttypeheader">
<insta:ltext key="storemgmt.pharmacyindentlist.list.indenttype"/>
</c:set>
<c:set var="raiseddate">
<insta:ltext key="storemgmt.pharmacyindentlist.list.raiseddate"/>
</c:set>
<c:set var="raiseUserindent">
<insta:ltext key="storemgmt.pharmacyindentlist.list.raise.userindent"/>
</c:set>
<c:set var="raiseTransferindent">
<insta:ltext key="storemgmt.pharmacyindentlist.list.raise.transferindent"/>
</c:set>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<h1><insta:ltext key="storemgmt.pharmacyindentlist.list.myindentsdashboard"/></h1>

<insta:feedback-panel/>

<form name="indentSearchForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="indentSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storemgmt.pharmacyindentlist.list.indentno"/></div>
			<div class="sboFieldInput">
				<input type="text" name="indent_no" id="indent_no" value="${ifn:cleanHtmlAttribute(param.indent_no)}" onkeypress="return enterNumOnlyzeroToNine(event);"/>
				<input type="hidden" name="indent_no@type" value="integer" />
			</div>
	  	</div>
		<c:choose>
			<c:when test="${roleId eq '1' || roleId eq '2'}">
			  	<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="storemgmt.pharmacyindentlist.list.user"/></div>
						<div class="sboFieldInput">
							<div id="userName_wrapper" style="width: 15em;">
								<input type="text" name="requester_name" id="requester_name" style="width: 15em;" value="${ifn:cleanHtmlAttribute(param.requester_name)}"/>
							<div id="userName_dropdown"></div>
						</div>
			    	</div>
			  	</div>
			 </c:when>
			 <c:otherwise>
			 	<input type="hidden" name="requester_name" id="requester_name" value="${ifn:cleanHtmlAttribute(param.requester_name)}"/>
			 </c:otherwise>
		 </c:choose>
	  </div>

	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
			<tr>
				<td>
					<div class="sfLabel"><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.raiseddate"/></div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="storemgmt.pharmacyindentlist.list.from"/>:</div>
						<insta:datewidget name="date_time" id="date_time0" value="${paramValues.date_time[0]}"/>
						</div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="storemgmt.pharmacyindentlist.list.to"/>:</div>
						<insta:datewidget name="date_time" id="date_time1" value="${paramValues.date_time[1]}"/>
						<input type="hidden" name="date_time@op" value="ge,le"/>
						<input type="hidden" name="date_time@type" value="date"/>
						<input type="hidden" name="date_time@cast" value="y"/>
					</div>
				</td>
				<td>
					<div class="sfLabel"><insta:ltext key="storemgmt.pharmacyindentlist.list.expecteddate"/></div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="storemgmt.pharmacyindentlist.list.from"/>:</div>
						<insta:datewidget name="expected_date" id="expected_date0" value="${paramValues.expected_date[0]}"/>
						</div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="storemgmt.pharmacyindentlist.list.to"/>:</div>
						<insta:datewidget name="expected_date" id="expected_date1" value="${paramValues.expected_date[1]}"/>
						<input type="hidden" name="expected_date@op" value="ge,le"/>
						<input type="hidden" name="expected_date@type" value="date"/>
						<input type="hidden" name="expected_date@cast" value="y"/>
					</div>
				</td>
				<c:if test="${multiCentered && centerId == 0}">
				<td>
					<div class="sfLabel"><insta:ltext key="storemgmt.pharmacyindentlist.list.requestingcenter"/></div>
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
				</c:if>
				<td>
					<div class="sfLabel"><insta:ltext key="storemgmt.pharmacyindentlist.list.status"/></div>
					<div class="sfField">
						<insta:checkgroup name="status" selValues="${paramValues.status}" opvalues="O,A,R,X,P,C" optexts="${status}"/>
					</div>
				</td>
				<td>
					<div class="sfLabel"><insta:ltext key="storemgmt.pharmacyindentlist.list.indenttype"/></div>
					<div class="sfField">
						<insta:checkgroup name="indent_type" optexts="${indenttype}" opvalues="S,U" selValues="${paramValues.indent_type}"/>
					</div>
				</td>
				<td>
					<div class="sfLabel"><insta:ltext key="storemgmt.pharmacyindentlist.list.itemname"/></div>
					<div class="sfField">
						<div id="itemWrapper">
							<input type="text" name="item_name" id="item_name" value="${ifn:cleanHtmlAttribute(param.item_name)}"/>
							<input type="hidden" name="item_name@op" value="co"/>
							<div id="item_dropdown" class="scrolForContainer"></div>
						</div>
					</div>
				</td>
			</tr>
		</table>
	  </div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<insta:sortablecolumn name="indent_no" title="${indentno}"/>
				<insta:sortablecolumn name="indent_type" title="${indenttypeheader}"/>
				<c:if test="${multiCentered && centerId == 0}">
					<th><insta:ltext key="storemgmt.pharmacyindentlist.list.requestingcenter"/></th>
				</c:if>
				<insta:sortablecolumn name="date_time" title="${raiseddate}"/>
				<th><insta:ltext key="storemgmt.pharmacyindentlist.list.status"/></th>
				<th><insta:ltext key="storemgmt.pharmacyindentlist.list.approvedby"/></th>

			</tr>
			<c:forEach var="indent" items="${indentList}" varStatus="st">
			<c:set var="view" value="${(indent.status ne 'O') || (indent.requester_name != username && (roleId ne '1' && roleId ne '2')) || (multiCentered && centerId == 0)} "/>

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
					<td><fmt:formatDate value="${indent.date_time }" pattern="dd-MM-yyyy"/></td>
				    <td>
						${statusDisplay[indent.status]}
					</td>
					<td>${indent.approved_by }</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
    </div>

	<div class="screenActions">
		<insta:screenlink screenId="stores_user_indent" extraParam="?_method=add" label="${raiseUserindent}"/>
		<insta:screenlink screenId="stores_transfer_indent" extraParam="?_method=add" addPipe="true" label="${raiseTransferindent}"/>
	</div>


</form>
</body>
</html>
