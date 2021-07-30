<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Asset Complaints List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<script>
	var multiStoreAccess = ${roleId ==1 || roleId==2 || (multiStoreAccess == 'A')};
	var gRoleId = '${ifn:cleanJavaScript(roleId)}';
	function init() {
		createToolbar(toolbar);
	}
	function checkstoreallocation() {
		if (gRoleId != 1 && gRoleId != 2) {
		var asset_dept = document.getElementById('asset_dept');
		if (multiStoreAccess) {
		// here it is a select box.
			if (asset_dept.options.length == 0) {
				alert("There is no assigned store, hence you dont have any access to this screen");
				document.getElementById("storecheck").style.display = 'none';
				return false;
			}
		} else {
		// here it is a hidden field
				if (asset_dept.value == '') {
					alert("There is no assigned store, hence you dont have any access to this screen");
					document.getElementById("storecheck").style.display = 'none';
					return false;
				}
			}
		}
	return true;
	}
	var toolbar = {
		Edit: {
			title: 'View/Edit',
			imageSrc: 'icons/Edit.png',
			href: 'resourcemanagement/AssetComplaints.do?method=show'
		}
	}
</script>
<style type="text/css">
	.status_InActive{background-color: #E4C89C}
</style>
</head>
<body onload="checkstoreallocation(); init(); showFilterActive(document.validateSearch); ">
<c:set var="filterclosed" value="${not empty pagedList.dtoList}"></c:set>
<div id="storecheck" style="display: block;" >
<form name="validateSearch">
<input type="hidden" name="method" value="list">
<input type="hidden" name="_method" value="list">
<input type="hidden" name="_searchMethod" value="list"/>
<h1>Asset Complaints</h1>
	<insta:feedback-panel/>

	<insta:search form="validateSearch" optionsId="optionalFilter" closed="${filterclosed}">
		<div class="searchBasicOpts" >
			<table class="searchFormTable">
			<tr>
				<td>
					<div class="sboField">
						<div class="sboFieldLabel">Asset:</div>
						<div class="sfField">
						<input type="text" name="medicine_name" value="${ifn:cleanHtmlAttribute(param.medicine_name)}"/>
						<input type="hidden" name="medicine_name@op" value="like" />
					</div>
					</div>
				</td>
				<td>
					<div class=:sboField">
					<div class="sboFieldLabel">Store:</div>
					<c:choose>
				 		<c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
						<insta:userstores username="${userid}" elename="asset_dept" val="${param.asset_dept}" id="asset_dept" defaultVal="-- Select --"/>
								<input type="hidden" name="asset_dept@type"  value="integer" />
							    <input type="hidden" name="asset_dept@cast"  value="y" />
						</c:when>
						<c:otherwise>
							<b><insta:getStoreName store_id="${dept_id}"/></b>
							<input type="hidden" name="asset_dept" id="asset_dept" value="${dept_id}" />
							<input type="hidden" name="asset_dept@type"  value="integer" />
							<input type="hidden" name="asset_dept@cast"  value="y" />
						</c:otherwise>
					</c:choose>
					</div>
				</td>
				<td class="last">
					<div class="sfLabel">Raised Date:</div>
					<div class="sfField">
						<div class="sfFieldSub">From:</div>
							<insta:datewidget name="raised_date" id="raised_date0"
													value="${paramValues.raised_date[0]}" calButton="true"/>
					</div>
					<div class="sfField">
						<div class="sfFieldSub">To:</div>
							<insta:datewidget name="raised_date" id="raised_date1"
								value="${paramValues.raised_date[1]}" calButton="true"/>
							<input type="hidden" name="raised_date@op" value="ge,le"/>
							<input type="hidden" name="raised_date@cast" value="y"/>
					</div>
				</td>
				<td class="last">
					&nbsp;
				</td>
				<td class="last">
					&nbsp;
				</td>
		    </tr>
		    </table>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${filterclosed ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
				    <td>
					<div class="sfLabel">Resolved Date:</div>
					<div class="sfField">
						<div class="sfFieldSub">From:</div>
							<insta:datewidget name="resolved_date" id="resolved_date0"
													value="${paramValues.resolved_date[0]}" calButton="true"/>
					</div>
					<div class="sfField">
						<div class="sfFieldSub">To:</div>
							<insta:datewidget name="resolved_date" id="resolved_date1"
								value="${paramValues.resolved_date[1]}" calButton="true"/>
						<input type="hidden" name="resolved_date@op" value="ge,le"/>
						<input type="hidden" name="resolved_date@cast" value="y"/>
					</div>
					</td>
					<td>
						<div class="sfLabel">Status:</div>
						<div class="sfField">

							<insta:checkgroup name="complaint_status" opvalues="0,1,2,3" optexts="Recorded,Assigned,Resolved,Closed" selValues="${paramValues.complaint_status}"/>
								<input type="hidden" name="complaint_status@op" value="in" />
								<input type="hidden" name="complaint_status@type" value="integer" />
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">Type:</div>
						<div class="sfField">
							<insta:selectoptions name="complaint_type" id="complaint_type"
								opvalues="empty,Completely down, Intermittent failure, Part failure, Suggestion"
					 			optexts="--Select--,Completely down, Intermittent failure, Part failure, Suggestion"
								value="${param.complaint_type}" dummyvalueId="*"/>
						</div>
					</td>
					<td class="last">
						&nbsp;
					</td>
					<td class="last">
						&nbsp;
					</td>
				</tr>
			</table>
		</div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<table class="formTable" align="center" style="width: 100%">

		<tr>
			<td colspan="2" align="center">
				<table class="resultList dialog_displayColumns" width="100%" id="recordsTable">
					<tr>
						<insta:sortablecolumn name="complaint_id" title="Complaint ID"/>
						<insta:sortablecolumn name="asset_name" title="Asset"/>
						<insta:sortablecolumn name="emp_name" title="User"/>
						<insta:sortablecolumn name="complaint_type" title="Type"/>
						<insta:sortablecolumn name="complaint_desc" title="Complaint"/>
						<insta:sortablecolumn name="complaint_status" title="Status"/>
						<insta:sortablecolumn name="dept_name" title="Store"/>
						<insta:sortablecolumn name="raised_date" title="Raised On"/>
					</tr>
					<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'recordsTable',
							{asset_name: '${record.map['asset_name']}' , batch_no: '${record.map['batch_no']}',
							complaint_id: '${record.map['complaint_id']}'});"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
						<td>${record.map['complaint_id']}</td>
						<td>${record.map['asset_str']}</td>
						<td>${record.map['emp_name']}</td>
						<td>${record.map['complaint_type']}</td>
						<td>${record.map['complaint_desc']}</td>
						<td><c:if test="${record.map['complaint_status'] == '0'}">RECORDED</c:if>
							<c:if test="${record.map['complaint_status'] == '1'}">ASSIGNED</c:if>
							<c:if test="${record.map['complaint_status'] == '2'}">RESOLVED</c:if>
							<c:if test="${record.map['complaint_status'] == '3'}">CLOSED</c:if>
						</td>
						<td>${record.map['dept_name']}</td>
						<td><fmt:formatDate value="${record.map['raised_date']}"/></td>
					</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
	</table>
	<insta:noresults hasResults="${filterclosed}" />

	<div class="screenActions">
		<c:url var="url" value="AssetComplaints.do">
			<c:param name="method" value="add"/>
		</c:url>
		<a href="<c:out value='${url}' />">Add New Complaint</a>
	</div>
</form>
</div>
</body>
</html>
