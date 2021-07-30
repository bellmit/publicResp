<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Fixed Assets List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		function init() {
			createToolbar(toolbar);
		}
		var toolbar = {
			Edit: {
				title: 'View/Edit',
				imageSrc: 'icons/Edit.png',
				href: '/master/FixedAssetMaster.do?method=show'
			}
		}
	</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<body onload="init();storeAccess();">
<c:set var="filterclosed" value="${not empty pagedList.dtoList}"></c:set>
<div id="storecheck" style="display:none;">
	<h1>Fixed Asset List</h1>
	<insta:feedback-panel/>

<form name="validateSearch">
	<input type="hidden" name="method" value="list">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list">
	<insta:search form="validateSearch" optionsId="optionalFilter" closed="${filterclosed}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Category:</div>
				<div class="sboFieldInput">
					<insta:selectdb  name="category_id" value="${param.category_id}" table="store_category_master" valuecol="category_id" displaycol="category"
				      filtercol="asset_tracking" filtervalue="Y" dummyvalue="----(All)-----" dummyvalueId=""/>
				      <input type="hidden" name="category_id@type"  value="integer" />
					<input type="hidden" name="category_id@cast" value="y" />
				</div>
			</div>
			<div class=:sboField">
			<div class="sboFieldLabel">Store:</div>
			<c:choose>
		 		<c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
					<insta:userstores username="${userid}" elename="dept_id" val="${dept_id}" id="dept_id" defaultVal="-- Select --"/>
					<input type="hidden" name="dept_id@type" value="integer" />
					<input type="hidden" name="dept_id@cast" value="y" />
				</c:when>
				<c:otherwise>
					<b><insta:getStoreName store_id="${dept_id}"/></b>
					<input type="hidden" name="dept_id" id="dept_id" value="${dept_id}" />
					<input type="hidden" name="dept_id@type" value="integer" />
					<input type="hidden" name="dept_id@cast" value="y" />
				</c:otherwise>
			</c:choose>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${filterclosed ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Status:</div>
						<div class="sfField">
								<insta:checkgroup name="asset_status" opvalues="A,I,R" optexts="Active,Inactive,Retired" selValues="${paramValues.asset_status}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Make:</div>
						<div class="sfField">
							<input type="text" name="asset_make" value="${ifn:cleanHtmlAttribute(param.asset_make)}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Model:</div>
						<div class="sfField">
							<input type="text" name="asset_model" value="${ifn:cleanHtmlAttribute(param.asset_model)}"/>
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">Serial No:</div>
						<div class="sfField">
							<input type="text" name="asset_serial_no" value="${ifn:cleanHtmlAttribute(param.asset_serial_no)}"/>
						</div>
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
				<table class="dashboard" width="100%" id="recordsTable">
					<tr>
						<insta:sortablecolumn name="medicine_name" title="Asset"/>
						<th>Make</th>
						<th>Model</th>
						<th>Serial No</th>
						<th>Category</th>
						<th>Store</th>
						<th>Status</th>
					</tr>
					<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'recordsTable',
							{asset_seq: '${record.asset_seq}'});"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">

						<c:choose>
							<c:when test="${record.asset_status eq 'I'}">
								<c:set var="flagColor" value="red"/>
							</c:when>
							<c:when test="${record.asset_status eq 'R'}">
								<c:set var="flagColor" value="grey"/>
							</c:when>
							<c:otherwise>
								<c:set var="flagColor" value="green"/>
							</c:otherwise>
						</c:choose>

						<td><img src="${cpath}/images/${flagColor}_flag.gif"/>${record.medicine_name}</td>
						<td>${record.asset_make}</td>
						<td>${record.asset_model}</td>
						<td>${record.asset_serial_no}</td>
						<td>${record.category}</td>
						<td>${record.dept_name}</td>
						<td>
							<c:if test="${record.asset_status eq 'I'}">Inactive</c:if>
							<c:if test="${record.asset_status eq 'A'}">Active</c:if>
							<c:if test="${record.asset_status eq 'R'}">Retired</c:if>
						</td>
					</tr>
					</c:forEach>
				</table>
			</td>
		</tr>

		<c:url var="url" value="FixedAssetMaster.do">
			<c:param name="method" value="add"/>
		</c:url>
		<!--
		<tr>
			<td colspan="2" align="left"><a href="${url}">Add New Asset</a></td>
		</tr>
		-->
	</table>
	<table align="right">
		<c:if test="${not empty pagedList.dtoList}">
		<tr>
			<td><img class="flag" src="${cpath}/images/green_flag.gif"/></td>
			<td>Active&nbsp;</td>
			<td>&nbsp;</td>
			<td><img class="flag" src="${cpath}/images/red_flag.gif"/></td>
			<td>InActive</td>
			<td>&nbsp;</td>
			<td><img class="flag" src="${cpath}/images/grey_flag.gif"/></td>
			<td>Retired</td>
		</tr>
		</c:if>
	</table>
</form>
</div>

<script type="text/javascript">
	function storeAccess(){
		var assignedStore = "${ifn:cleanJavaScript(dept_id)}";
		if (${roleId != 1 && roleId != 2}) {
			if (assignedStore == '' || assignedStore == null){
				alert('You do not have any assigned store, hence you are not allowed to access this screen');
				document.getElementById("storecheck").style.display='none';
				return false;
			} else{
				document.getElementById("storecheck").style.display='block';
			}
		} else{
			document.getElementById("storecheck").style.display='block';
		}
	}
</script>
</body>
</html>
