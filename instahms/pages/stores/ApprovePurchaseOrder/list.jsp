<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title><insta:ltext key="storeprocurement.poapprovaldashboard.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="sales.issues"/>
	<script>
	var toolbarOptions = getToolbarBundle("js.sales.issues.toolbar");
	var deptId = '${ifn:cleanJavaScript(store_id)}';
	var gRoleId = '${roleId}';
	var accessstores = '${multiStoreAccess ? multiStoreAccess : N}';
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/view_approval_po.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:js-bundle prefix="stores.procurement"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="poAppList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty poAppList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="O" value="Open"/>
<c:set target="${statusDisplay}" property="V" value="Validated"/>
<c:set target="${statusDisplay}" property="A" value="Approved"/>
<c:set target="${statusDisplay}" property="AO" value="Amended Open"/>
<c:set target="${statusDisplay}" property="AV" value="Amended Validated"/>
<c:set target="${statusDisplay}" property="AA" value="Amended Approved"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="FC" value="Force Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>
<c:set var="validatePOReq" value="${genPrefs.poToBeValidated == 'Y'}"/>

<body onload="init();checkstoreallocation();">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="all">
<insta:ltext key="storeprocurement.poapprovaldashboard.list.all.in.brackets"/>
</c:set>
<c:set var="pono">
<insta:ltext key="storeprocurement.poapprovaldashboard.list.pono"/>
</c:set>
<c:set var="supplierTitle">
<insta:ltext key="storeprocurement.poapprovaldashboard.list.supplier"/>
</c:set>
<c:set var="raisedno">
<insta:ltext key="storeprocurement.poapprovaldashboard.list.raisedby"/>
</c:set>
<c:set var="raiseddate">
<insta:ltext key="storeprocurement.poapprovaldashboard.list.raiseddate"/>
</c:set>
<c:set var="status2">
<insta:ltext key="storeprocurement.poapprovaldashboard.list.open"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.approved"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.closed"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.amended.open"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.amended.approved"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.forceclosed"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.cancelled"/>
</c:set>
<c:set var="status1">
<insta:ltext key="storeprocurement.poapprovaldashboard.list.open"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.validated"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.approved"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.closed"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.amended.open"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.amended.validated"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.amended.approved"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.forceclosed"/>,
<insta:ltext key="storeprocurement.poapprovaldashboard.list.cancelled"/>
</c:set>
<h1><insta:ltext key="storeprocurement.poapprovaldashboard.list.poapprovaldashboard"/></h1>

<insta:feedback-panel/>
<div id="storecheck" style="display: block;" >
<form name="poApprovalSearchForm" action="StoresPOApproval.do" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

<insta:search form="poApprovalSearchForm" optionsId="optionalFilter" closed="${hasResults}">
	<div class="searchBasicOpts" >

		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storeprocurement.poapprovaldashboard.list.supplier"/>:</div>
			<div class="sboFieldInput">
				<select name="supplier_id" id="supplier_id" class="dropdown">
					<option value="">---(All)---</option>
						<c:forEach items="${listcentersforsuppliers}" var="supplier">
							<option value="${supplier.map.supplier_code}"  ${supplier.map.supplier_code == param.supplier_id ? 'selected': ''}>${supplier.map.supplier_name}</option>
						</c:forEach>
				</select>
			</div>
		</div>
	</div>
	<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	<table class="searchFormTable">
	<tr>
		<td>
			<div class="sfLabel"><insta:ltext key="storeprocurement.poapprovaldashboard.list.store"/>:</div>
			<c:choose>
			<c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
				<div class="sfField">
					<insta:userstores username="${userid}" elename="store_id" id="store_id" />
					<input type="hidden" name="store_id@type" value="integer"/>
				</div>
			</c:when>
			<c:otherwise>
				<input type="hidden" name="store_id@type" value="integer"/>
				<insta:userstores username="${userid}" elename="store_id" id="store_id" disabled="disabled"/>
			</c:otherwise>
			</c:choose>
		</td>
		<td>
			<div class="sfLabel"><insta:ltext key="storeprocurement.poapprovaldashboard.list.raisedby"/>:</div>
			<div class="sfField" >
	  				<insta:selectoptions name="user_id" value="${param.user_id}" opvalues="${store_usersWithAutoPO}" optexts="${store_usersWithAutoPO}" dummyvalue="${dummyvalue}"/>
			</div>
		
			<div class="sfLabel"><insta:ltext key="storeprocurement.poapprovaldashboard.list.pono"/>:</div>
			<div class="sfField">
					<input type="text" name="po_no" value="${ifn:cleanHtmlAttribute(param.po_no)}">
			</div>
		</td>
		<td>
			<div class="sfLabel"><insta:ltext key="storeprocurement.poapprovaldashboard.list.raiseddate"/>:</div>
			<div class="sfField">
				<div class="sfFieldSub"><insta:ltext key="storeprocurement.poapprovaldashboard.list.from"/>:</div>
				<insta:datewidget name="po_date" id="po_date0" value="${paramValues.po_date[0]}"/>
				</div>
			<div class="sfField">
				<div class="sfFieldSub"><insta:ltext key="storeprocurement.poapprovaldashboard.list.to"/>:</div>
				<insta:datewidget name="po_date" id="po_date1" value="${paramValues.po_date[1]}"/>
				<input type="hidden" name="po_date@op" value="ge,le"/>
			</div>
		</td>
		<td>
			<div class="sfLabel"><insta:ltext key="storeprocurement.poapprovaldashboard.list.status"/>:</div>
			<div class="sfField">
				<c:choose>
					<c:when test="${validatePOReq}">
						<insta:checkgroup name="status" selValues="${paramValues.status}"
								opvalues="O,V,A,C,AO,AV,AA,FC,X" optexts="${status1}"/>
					</c:when>
					<c:otherwise>
						<insta:checkgroup name="status" selValues="${paramValues.status}"
								opvalues="O,A,C,AO,AA,FC,X" optexts="${status2}"/>
					</c:otherwise>
				</c:choose>

			</div>
		</td>
		</tr>
		</table>
	</div>
</insta:search>
<c:if test="${pagedList.totalRecords gt 0 }">
<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<insta:sortablecolumn name="po_no" title="${pono}"/>
				<insta:sortablecolumn name="supplier_name" title="${supplierTitle}"/>
				<insta:sortablecolumn name="user_id" title="${raisedno}"/>
				<insta:sortablecolumn name="po_date" title="${raiseddate}"/>
				<th><insta:ltext key="storeprocurement.poapprovaldashboard.list.status"/></th>
				<th><insta:ltext key="storeprocurement.poapprovaldashboard.list.approvedby"/></th>
			</tr>
			<c:forEach var="po" items="${poAppList}" varStatus="st">
			<c:set var="view" value="${( validatePOReq ? (po.status ne 'V' && po.status ne 'AV') : (po.status ne 'O' && po.status ne 'AO') )}"/>
			<c:set var="edit">
					<c:choose>
						<c:when test="${( validatePOReq ? (po.status eq 'V' || po.status eq 'AV') : (po.status eq 'O' || po.status eq 'AO') ) && (userId eq 'InstaAdmin' || userId eq 'admin')}"><insta:ltext key="storeprocurement.poapprovaldashboard.list.true"/></c:when>
						<c:when test="${( validatePOReq ? (po.status eq'V' || po.status eq 'AV') : (po.status eq 'O' || po.status eq 'AO') ) && (not empty userpolimit ? userpolimit ge po.po_total : true)}"><insta:ltext key="storeprocurement.poapprovaldashboard.list.true"/></c:when>
						<c:otherwise><insta:ltext key="storeprocurement.poapprovaldashboard.list.false"/></c:otherwise>
					</c:choose>
			</c:set>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{po_no:'${po.po_no }'},
						[${(edit)},${view}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${po.po_no}</td>
					<td>${po.supplier_name}</td>
					<td>${po.user_id}</td>
					<td><fmt:formatDate value="${po.po_date}" pattern="dd-MM-yyyy"/></td>
				    <td>${statusDisplay[po.status]}</td>
					<td>${po.approved_by }</td>
				</tr>
			</c:forEach>
		</table>

		

    </div>
	</c:if>
		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
 </form>

 </div>
</body>
</html>