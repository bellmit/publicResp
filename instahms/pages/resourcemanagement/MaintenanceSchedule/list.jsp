<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Maint. Schedule List - Insta HMS</title>
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
			href: '/resourcemanagement/MaintenanceSchedule.do?method=show'
		}
	}
</script>
</head>
<body onload="checkstoreallocation(); init(); showFilterActive(document.validateSearch);">
<c:set var="filterclosed" value="${not empty pagedList.dtoList}"></c:set>
<h1>Maintenance Schedule Master</h1>
<div id="storecheck" style="display: block;" >
<form name="validateSearch">
	<input type="hidden" name="method" value="list">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list">

		<insta:search form="validateSearch" optionsId="optionalFilter" closed="${filterclosed}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Asset:</div>
					<div class="sboFieldInput">
						<input type="text" name="medicine_name" value="${ifn:cleanHtmlAttribute(param.medicine_name)}"/>
						<input type="hidden" name="medicine_name@op" value="ilike"/>
					</div>
				</div>
				<div class="sboField">
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
			</div>
			<div id="optionalFilter" style="clear: both; display: ${filterclosed ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Frequency:</div>
							<div class="sfField">
								<insta:selectoptions name="maint_frequency" id="maint_frequency"
									opvalues="Weekly,Every two weeks,Monthly,Quarterly,Semi-annually,Annually,Custom"
									optexts="Weekly,Every two weeks,Monthly,Quarterly,Semi-annually,Annually,Custom"
									value="${param.maint_frequency}" dummyvalue="--Select--"/>
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Next Maintenance Date:</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>
								<insta:datewidget name="next_maint_date" id="next_maint_date0"
												value="${paramValues.next_maint_date[0]}" calButton="true"/>
							</div>
							<div class="sfField">
								<div class="sfFieldSub">To:</div>
								<insta:datewidget name="next_maint_date" id="next_maint_date1"
												value="${paramValues.next_maint_date[1]}" calButton="true"/>
								<input type="hidden" name="next_maint_date@op" value="ge,le"/>
								<input type="hidden" name="next_maint_date@cast" value="y"/>
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

	<table class="formTable" align="center" style="width: 100%">

		<tr>
			<td colspan="2" align="center">
				<table class="resultList dialog_displayColumns" width="100%" id="recordsTable">
					<tr>
						<insta:sortablecolumn name="asset_name" title="Asset"/>
						<insta:sortablecolumn name="maint_frequency" title="Frequency"/>
						<insta:sortablecolumn name="next_maint_date" title="Next Maintenance Date"/>
						<insta:sortablecolumn name="contractor_name" title="Maint. Org"/>
						<insta:sortablecolumn name="department_contact" title="Contact No"/>
						<insta:sortablecolumn name="dept_name" title="Store"/>
					</tr>
					<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'recordsTable',
							{asset_name: '${record.map['asset_name']}' , batch_no: '${record.map['batch_no']}', maint_id: '${record.map['maint_id']}'});"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
						<td>${record.map['asset_str']}</td>
						<td>${record.map['maint_frequency']}</td>
						<td><fmt:formatDate value="${record.map['next_maint_date']}" pattern="dd-MM-yyyy"/></td>
						<td>${record.map['contractor_name']}</td>
						<td>${record.map['department_contact']}</td>
						<td>${record.map['dept_name']}</td>
					</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
	</table>
	<insta:noresults hasResults="${filterclosed}" />


		<c:url var="url" value="MaintenanceSchedule.do">
			<c:param name="method" value="add"/>
		</c:url>
		<div class="screenActions">
			<a href="<c:out value='${url}' />">Add New Maintenance Schedule</a>
		</div>
</form>
</div>
</body>
</html>
