<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title><insta:ltext key="storemgmt.indentapprovallist.list.transfer.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.mgmt.indent"/>
	<insta:js-bundle prefix="stores.mgmt"/>
	<script>
	var toolbarOptions = getToolbarBundle("js.stores.mgmt.indent.toolbar");
	var userNameList = <%= request.getAttribute("userNameList") %>;
	var isUserHavingSuperStore = <%= request.getAttribute("isUserHavingSuperStoreJson") %>;
 	var gRoleId = '${roleId}';
	</script>
	<script src="${cpath}/pages/stores/getItemMaster.do?ts=${master_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}"></script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/view_approval_indent.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>

	<style type="text/css">
		.scrolForContainer .yui-ac-content {
			 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    		_height:18em; max-width:35em; width:35em;/* ie6 */
		}
	</style>

</head>
<c:set var="indentApprovedBy"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("indent_approval_by") %>'
	scope="request" />
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
<c:set target="${indentDisplay}" property="S" value="Stock Transfer"/>
<c:set target="${indentDisplay}" property="U" value="Dept / Ward Issue"/>
<c:set var="allowCrossCenterIndents" value = "<%=GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents()%>" />
<body onload="init(); showFilterActive(document.indentApprovalSearchForm)">
<c:set var="indentno">
<insta:ltext key="storemgmt.indentapprovallist.list.indentno"/>
</c:set>
<c:set var="indenttype">
<insta:ltext key="storemgmt.indentapprovallist.list.indenttype"/>
</c:set>
<c:set var="requestername">
<insta:ltext key="storemgmt.indentapprovallist.list.raisedby"/>
</c:set>
<c:set var="raiseddate">
<insta:ltext key="storemgmt.indentapprovallist.list.raiseddate"/>
</c:set>
<c:set var="expecteddate">
<insta:ltext key="storemgmt.indentapprovallist.list.expecteddate"/>
</c:set>
<c:set var="all">
<insta:ltext key="storemgmt.indentapprovallist.list.all.in.brackets"/>
</c:set>
<c:set var="status">
<insta:ltext key="storemgmt.indentapprovallist.list.open"/>,
<insta:ltext key="storemgmt.indentapprovallist.list.approved"/>,
<insta:ltext key="storemgmt.indentapprovallist.list.rejected"/>,
<insta:ltext key="storemgmt.indentapprovallist.list.cancelled"/>,
<insta:ltext key="storemgmt.indentapprovallist.list.processed"/>,
<insta:ltext key="storemgmt.indentapprovallist.list.closed"/>
</c:set>
<c:set var="indenttype1">
<insta:ltext key="storemgmt.indentapprovallist.list.stocktransfer"/>,
<insta:ltext key="storemgmt.indentapprovallist.list.deptorwardissue"/>
</c:set>
<c:set var="dummyvalue">
<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<div id="storecheck" style="display: block;" >
<h1><insta:ltext key="storemgmt.indentapprovallist.list.indentapprovallist"/></h1>

<insta:feedback-panel/>

<form name="indentApprovalSearchForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="indentApprovalSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
	 		<div class="sboFieldLabel"><insta:ltext key="storemgmt.indentapprovallist.list.requestingstore"/></div>
	 		<c:choose>
				<c:when test="${indentApprovedBy == 'R'}">
					<div class="sboFieldInput">
					<insta:userstores username="${userid}" elename="dept_from" onlySuperStores="N" id="dept_from"  name="dept_from" val="${param.dept_from}" defaultVal="--All--" showDefaultValueForNormalUsers="Y"/>
					</div>
				</c:when>
				<c:when test="${indentApprovedBy == 'I'}">
					<div class="sboFieldInput">
							<insta:selectdb  name="dept_from" value="${param.dept_from}"
						table="stores" valuecol="dept_id" dummyvalue="${all}" displaycol="dept_name" orderby="dept_name" class="dropdown" />
					</div>
				</c:when>
			</c:choose>

		</div>
		<input type="hidden" name="dept_from@type" value="string"/>
		<input type="hidden" name="dept_from@cast" value="y"/>
		 	<div class="sboField">
			 	<div class="sboFieldLabel"><insta:ltext key="storemgmt.indentapprovallist.list.indentstore"/></div>
			 	<c:choose>
					<c:when test="${indentApprovedBy == 'R'}">
						<div class="sboFieldInput">
							<c:choose>
								<c:when test="${allowCrossCenterIndents == 'Y'}">
									<insta:selectdb  name="indent_store" value="${param.indent_store}"
									table="stores" id ="indent_store" valuecol="dept_id" dummyvalue="${all}" displaycol="dept_name"
									filtercol="is_super_store,status" filtervalue ="Y,A" orderby="dept_name" class="dropdown" />
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
					</c:when>
					<c:when test="${indentApprovedBy == 'I'}">
					<div class="sboFieldInput">
						<insta:userstores username="${userid}" elename="indent_store" onlySuperStores="Y" id="indent_store" val="${param.indent_store}" defaultVal="--All--" showDefaultValueForNormalUsers="Y"/>
					</div>
					</c:when>
				</c:choose>
			</div>
			<input type="hidden" name="indent_store@type" value="string"/>
			<input type="hidden" name="indent_store@cast" value="y"/>
			<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storemgmt.indentapprovallist.list.indentno"/></div>
				<div class="sboFieldInput">
					<input type="text" name="indent_no" id="indent_no" value="${ifn:cleanHtmlAttribute(param.indent_no)}" onkeypress="return enterNumOnlyzeroToNine(event);"/>
					<input type="hidden" name="indent_no@type" value="integer" />
				</div>
	  	</div>
		</div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.indentapprovallist.list.expecteddate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storemgmt.indentapprovallist.list.from"/>:</div>
							<insta:datewidget name="expected_date" id="expected_date0" value="${paramValues.expected_date[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storemgmt.indentapprovallist.list.to"/>:</div>
							<insta:datewidget name="expected_date" id="expected_date1" value="${paramValues.expected_date[1]}"/>
							<input type="hidden" name="expected_date@op" value="ge,le"/>
							<input type="hidden" name="expected_date@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.indentapprovallist.list.raiseddate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storemgmt.indentapprovallist.list.from"/>:</div>
							<insta:datewidget name="date_time" id="date_time0" value="${paramValues.date_time[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storemgmt.indentapprovallist.list.to"/>:</div>
							<insta:datewidget name="date_time" id="date_time1" value="${paramValues.date_time[1]}"/>
							<input type="hidden" name="date_time@op" value="ge,le"/>
							<input type="hidden" name="date_time@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.indentapprovallist.list.status"/></div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="O,A,R,X,P,C" optexts="${status}"/>
						</div>
					</td>
					<%-- we require only stock transfer records. hence we are putting indent_type="S" --%>
						<input type="hidden" name ="indent_type" value ="S"/>

					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.indentapprovallist.list.itemname"/></div>
						<div class="sfField">
							<div id="itemWrapper">
								<input type="text" name="item_name" id="item_name" value="${ifn:cleanHtmlAttribute(param.item_name)}"/>
								<input type="hidden" name="item_name@op" value="ilike" />
								<div id="item_dropdown" class="scrolForContainer"></div>
							</div>
						</div>
					</td>
				</tr>
				<c:if test="${multiCentered && centerId == 0}">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.indentapprovallist.list.requestingcenter"/></div>
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

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<insta:sortablecolumn name="indent_no" title="${indentno}"/>
				<th><insta:ltext key="storemgmt.indentapprovallist.list.requestingstore"/></th>
				<c:if test="${multiCentered && centerId == 0}">
					<th><insta:ltext key="storemgmt.indentapprovallist.list.requestingcenter"/></th>
				</c:if>
				<insta:sortablecolumn name="indent_type" title="${indenttype}"/>
				<insta:sortablecolumn name="requester_name" title="${requestername}"/>
				<insta:sortablecolumn name="date_time" title="${raiseddate}"/>
				<insta:sortablecolumn name="expected_date" title="${expecteddate}"/>
				<th><insta:ltext key="storemgmt.indentapprovallist.list.status"/></th>
				<th><insta:ltext key="storemgmt.indentapprovallist.list.approvedby"/></th>
			</tr>
			<c:forEach var="indent" items="${indentAppList}" varStatus="st">
			<c:set var="view" value="${indent.status ne 'O'}"/>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{indent_no:'${indent.indent_no }'},
						[${view },!${view},true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${indent.indent_no}</td>
					<td>${indent.dept_from_name}</td>
					<c:if test="${ multiCentered && centerId == 0 }">
						<td><insta:getCenterName center_id="${indent.requesting_center_id}"/></td>
					</c:if>
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
 </div>
</body>
</html>
