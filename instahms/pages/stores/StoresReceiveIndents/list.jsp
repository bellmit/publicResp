<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>
<head>
	<title><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="sales.issues.receivedindent"/>
	<insta:js-bundle prefix="stores.procurement"/>
   	<insta:js-bundle prefix="stores.mgmt"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.sales.issues.receivedindent.toolbar");
		var userNameList = <%= request.getAttribute("userNameList") %>;
		var gAccessAllow = '${genPrefs.recTransIndent}';
	</script>
	<insta:link type="css"  file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/view_receive_indent.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="indentApprovedBy" value='<%= GenericPreferencesDAO.getAllPrefs().get("indent_approval_by") %>' scope="request" />
<c:set var="allowCrossCenterIndents" value = "<%=GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents()%>" />
<c:set var="indentAppList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty indentAppList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="O" value="Open"/>
<c:set target="${statusDisplay}" property="A" value="Approved"/>
<c:set target="${statusDisplay}" property="R" value="Rejected"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>
<c:set target="${statusDisplay}" property="P" value="Processed"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<jsp:useBean id="indentDisplay" class="java.util.HashMap"/>
<c:set target="${indentDisplay}" property="R" value="Request for new Items"/>
<c:set target="${indentDisplay}" property="S" value="Stock Transfer"/>
<c:set target="${indentDisplay}" property="U" value="Dept / Ward Issue"/>
<body onload="init(); showFilterActive(document.indentReceiveSearchForm);">
<c:set var="indentno">
<insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.indentno"/>
</c:set>
<c:set var="indenttype">
<insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.indenttype"/>
</c:set>
<c:set var="raisedby">
<insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.raisedby"/>
</c:set>
<c:set var="raiseddate">
<insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.raiseddate"/>
</c:set>
<c:set var="expecteddate">
<insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.expecteddate"/>
</c:set>
<c:set var="all">
<insta:ltext key="storemgmt.processindentlist.list.all"/>
</c:set>

<div id="storecheck" style="display: block;" >
<h1><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.receivetransferedindentsdashboard"/></h1>

<insta:feedback-panel/>

<form name="indentReceiveSearchForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="indentReceiveSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <table class="searchBasicOpts" >
		  <tr>
		  	<td>
		  		<div class="sboField">
		  		<div class="sboFieldLabel"><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.raisedbyusername"/></div>
				<div class="sboFieldInput">
					<div id="userName_wrapper" style="width: 15em;">
						<input type="text" name="requester_name" id="requester_name" value="${ifn:cleanHtmlAttribute(param.requester_name)}"/>
					<div id="userName_dropdown"></div>
					</div>
	    		</div>
		  		</div>
		  	</td>
		  </tr>
	  	</table>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
	  			<tr>
	  				<td>
				  		<div class="sfLabel"><insta:ltext key="storemgmt.pharmacyindentlist.list.indentno"/></div>
						<div class="sfField">
							<input type="text" name="indent_no" id="indent_no" value="${ifn:cleanHtmlAttribute(param.indent_no)}"/>
							<input type="hidden" name="indent_no@type" value="integer" />
			    		</div>
				  	</td>
	  				<td>
				 		<div class="sfLabel"><insta:ltext key="storemgmt.indentapprovallist.list.requestingstore"/></div>
				 		<div class="sfField">
				 		<div class="sboFieldInput">
								<insta:userstores username="${userid}" elename="dept_from" onlySuperStores="N" id="dept_from"  name="dept_from" val="${param.dept_from}" defaultVal="--All--" showDefaultValueForNormalUsers="Y"/>
						</div>
						<input type="hidden" name="dept_from@type" value="string"/>
						<input type="hidden" name="dept_from@cast" value="y"/>
						</div>
	  				</td>
	  				<td>
	  					<div class="sfLabel"><insta:ltext key="storemgmt.indentapprovallist.list.indentstore"/></div>
	  					<div class="sfField">
					 	<div class="sboFieldInput">
							<c:choose>
								<c:when test="${allowCrossCenterIndents == 'Y'}">
									<insta:selectdb  name="indent_store" value="${param.indent_store}"
									table="stores" id ="indent_store" valuecol="dept_id" dummyvalue="${all}" displaycol="dept_name"
									filtercol="is_super_store" filtervalue ="Y" orderby="dept_name" class="dropdown" />
								</c:when>
								<c:otherwise>
									<select name="indent_store" id="indent_store" class = "dropdown">
			 							<option value="">${all}</option>
											<c:forEach var="stores" items="${storesList}">${stores.map.dept_id}
												<option value="${stores.map.dept_id}" ${indent_store == stores.map.dept_id ? 'selected':''}>${stores.map.dept_name}</option>
											</c:forEach>
									</select>
								</c:otherwise>
							</c:choose>
						</div>
						<input type="hidden" name="indent_store@type" value="string"/>
						<input type="hidden" name="indent_store@cast" value="y"/>
	  					</div>
	  				</td>
	  				<c:if test="${multiCentered && centerId == 0}">
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.pharmacyindentlist.list.requestingcenter"/></div>
						<div class="sfField">
							<select class="dropdown" name="requesting_center_id" id="requesting_center_id">
								<option value="">${all}</option>
								<c:forEach items="${centers}" var="center">
									<option value="${center.map.center_id}"
										${param.requesting_center_id == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
								</c:forEach>
							</select>
							<input type="hidden" name="requesting_center_id@cast" value="y"/>
						</div>
					</td>
					</c:if>
	  			</tr>
	  			
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.raiseddate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.from"/>:</div>
							<insta:datewidget name="date_time" id="date_time0" value="${paramValues.date_time[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.to"/>:</div>
							<insta:datewidget name="date_time" id="date_time1" value="${paramValues.date_time[1]}"/>
							<input type="hidden" name="date_time@op" value="ge,le"/>
							<input type="hidden" name="date_time@type" value="date"/>
							<input type="hidden" name="date_time@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.expecteddate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.from"/>:</div>
							<insta:datewidget name="expected_date" id="expected_date0" value="${paramValues.expected_date[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.to"/>:</div>
							<insta:datewidget name="expected_date" id="expected_date1" value="${paramValues.expected_date[1]}"/>
							<input type="hidden" name="expected_date@op" value="ge,le"/>
							<input type="hidden" name="expected_date@type" value="date"/>
							<input type="hidden" name="expected_date@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.processeddate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.from"/>:</div>
							<insta:datewidget name="processed_date" id="processed_date0" value="${paramValues.processed_date[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.to"/>:</div>
							<insta:datewidget name="processed_date" id="processed_date1" value="${paramValues.processed_date[1]}"/>
							<input type="hidden" name="processed_date@op" value="ge,le"/>
							<input type="hidden" name="processed_date@type" value="date"/>
							<input type="hidden" name="processed_date@cast" value="y"/>
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
				<insta:sortablecolumn name="indent_no" title="${indentno}"/>
				<insta:sortablecolumn name="indent_type" title="${indenttype}"/>
				<insta:sortablecolumn name="requester_name" title="${raisedby}"/>
				<insta:sortablecolumn name="date_time" title="${raiseddate}"/>
				<insta:sortablecolumn name="expected_date" title="${expecteddate}"/>
				<th><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.status"/></th>
				<th><insta:ltext key="storeprocurement.receivetransferedindentsdashboard.list.approvedby"/></th>
			</tr>
			<c:forEach var="indent" items="${indentAppList}" varStatus="st">
			<c:set var="view" value="${indent.status ne 'P' && indent.status ne 'A' }"/>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{indent_no:'${indent.indent_no }'},
						[${view },!${view}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${indent.indent_no}</td>
					<td>${indentDisplay[indent.indent_type]}</td>
					<td>${indent.requester_name}</td>
					<td><fmt:formatDate value="${indent.date_time }" pattern="dd-MM-yyyy"/></td>
					<td><fmt:formatDate value="${indent.expected_date }" pattern="dd-MM-yyyy"/></td>
				    <td>${statusDisplay[indent.status]}</td>
					<td>${indent.approved_by }</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

 </form>
 <script>
		var deptId = '${pharmacyStoreId}';
		var gRoleId = '${roleId}';

	</script>
</body>
</html>