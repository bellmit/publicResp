<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>Outsource Center Master - Insta HMS</title>
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="js" file="widgets.js"/>
		<insta:link type="script" file="dashboardColors.js"/>
		<insta:link type="js" file="dashboardsearch.js"/>
		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
		<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
		<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
		<c:set var="centralLabMod" value="${centralLabModule.map.activation_status}" />
		<script type="text/javascript">

			function deleteSelected(e) {
				var deleteEl = document.getElementsByName("_deleteCode");
				for (var i=0; i< deleteEl.length; i++) {
					if (deleteEl[i].checked) {
						document.forms[0]._method.value = 'delete';
						document.forms[0].submit();
						return true;
					}
				}
				alert("select at least one to delete");
				YAHOO.util.Event.stopEvent(e);
				return false;
			}

			function markDelete(obj) {
				var row = YAHOO.util.Dom.getAncestorByTagName(obj, 'tr');
				getElementByName(row, '_deleted').value = obj.checked ? 'Y' : 'N';
			}

		</script>
	</head>
	<body >
		<c:set var="hasResult" value="${not empty pagedList.dtoList ? 'true': 'false'}"/>
		<div class="pageHeader">Outsource Center Master</div>
		<insta:feedback-panel/>
		<form name="outhouseCenterForm" method="GET">

			<input type="hidden" name="_method" value="list"/>
			<input type="hidden" name="_searchMethod" value="list"/>

			<insta:search-lessoptions form="outhouseCenterForm" >

			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">Diag Outsource</div>
					<div class="sboFieldInput">
							<select name="outsource_dest" id="outsource_dest" Class="dropdown">
									<option value="">..Select..</option>
										<c:forEach var="outhouse" items="${outsourcedetail}">
											<option value="${outhouse.OUTSOURCE_DEST}"  ${param['outsource_dest'] == outhouse.OUTSOURCE_DEST ? 'selected' : ''}>
											${outhouse.OUTSOURCE_NAME}</option>
										</c:forEach>
								</select>
					</div>
				</div>
				<div class="sboField">
						<div class="sboFieldLabel">Center</div>
						<div class="sboFieldInput">
							<select name="center_id" id="center_id" class="dropdown">
								<option value="">-- All --</option>
								<c:forEach items="${centers}" var="center">
									<option value="${center.map.center_id}" ${param['center_id'] == center.map.center_id ? 'selected' : ''}>
										${center.map.center_name}
									</option>
								</c:forEach>
							</select>
							<input type="hidden" name="center_id@cast" value="y"/>
						</div>
				</div>
			</div>
			</insta:search-lessoptions>

			<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

			<div class="resultList">
				<table class="resultList" id="resultTable">
					<tr onmouseover="hideToolBar();">
							<th>Select</th>
							<th>Diag Outsource</th>
							<th>Center</th>
					</tr>
					<tr>
						<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index %2 == 0 ? 'even' : 'odd'}"
							id="toolbarRow${st.index}" >

							<td>
								<input type="checkbox" name="_deleteCode" onclick="markDelete(this);"/>
								<input type="hidden" name="_deleted" value="N" />
								<input type="hidden" name="_outsource_id" value="${record.outsource_id}" />
								<input type="hidden" name="_center_id" value="${record.center_id}" />
								<input type="hidden" name="_outsource_name" value="${record.outsource_name}"/>
								<input type="hidden" name="_center_name" value="${record.center_name}"/>
							</td>
							<td>${record.outsource_name}</td>
							<td>${record.center_name}</td>
						</tr>
							</c:forEach>
					</tr>
				</table>
			</div>

			<c:if test="${empty pagedList.dtoList}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

			<c:url var="url" value="CenterOuthousesAction.do">
				<c:param name="_method" value="add"/>
			</c:url>

			<c:url var="internalLabUrl" value="CenterOuthousesAction.do">
				<c:param name="_method" value="addCenterToInternalLab"/>
			</c:url>

			<div class="screenActions" style="float: left">
				<input type="button" name="delete" value="Delete" onclick="deleteSelected(event);"/> |
				<a href="<c:out value='${url}'/>">Add Outhouse Center Association</a>
				<c:if test="${max_centers_inc_default > 1 && isInternalLabExists && centralLabMod eq 'Y'}">
					| <a href="<c:out value='${internalLabUrl}'/>">Add Internal Lab center Association</a>
				</c:if>
			</div>
		</form>
		<div class="legend" >
		      <div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
		      <div class="flagText">Default Out Source</div>
	    </div>
	</body>
</html>
