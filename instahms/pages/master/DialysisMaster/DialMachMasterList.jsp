<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="generalmasters.dialysismachinemasters.list.dialysismachinemasterlist"/></title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
		.status_{background-color: #EAD6BB}
	</style>

	<insta:js-bundle prefix="dialysismodule.dialysismachinemaster"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.dialysismodule.dialysismachinemaster.toolbar");
	</script>

	<script>
		var toolbar = {
				Edit:{
				title: toolbarOptions["editvisit"]["name"],
				imageSrc: "icons/Edit.png",
				href: 'master/DialMachMaster.do?_method=show',
				onclick: null,
				description: toolbarOptions["editvisit"]["description"]
				}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.DiaMachineSearchForm);
		}
	</script>

	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>

</head>
<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
<c:set var="machinename">
 <insta:ltext key="generalmasters.dialysismachinemasters.list.machinename"/>
</c:set>
<c:set var="allText">
 <insta:ltext key="patient.dialysis.common.all"/>
</c:set>

<c:set var="status">
 <insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 <insta:ltext key="generalmasters.dialysismachinemasters.list.temporarilydown"/>,
 <insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>
</c:set>

<c:set var="locationname">
 <insta:ltext key="generalmasters.dialysismachinemasters.list.location"/>
</c:set>
	<h1><insta:ltext key="generalmasters.dialysismachinemasters.list.dialysismachinemaster"/></h1>

	<insta:feedback-panel/>

	<form name="DiaMachineSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortDReverse)}"/>

		<insta:search form="DiaMachineSearchForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="generalmasters.dialysismachinemasters.list.machinename"/></div>
					<div class="sboFieldInput">
						<input type="text" name="machine_name" value="${ifn:cleanHtmlAttribute(param.machine_name)}">
						<input type="hidden" name="machine_name@op" value="ico" />
					</div>
				</div>
			</div>
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="generalmasters.dialysismachinemasters.list.locationname"/></div>
					<div class="sboFieldInput">
						<select name="location_id" id="location_id" class="dropdown">
							<option value="">${allText}</option>
							<c:forEach items="${locations}" var="location">
								<option value="${location.location_id}"
									${param.location_id == location.location_id ? 'selected' : ''}>${location.location_name}</option>
							</c:forEach>
						</select>
						<input type="hidden" name="location_id@cast" value="y" />
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel"><insta:ltext key="generalmasters.dialysismachinemasters.list.networkaddress"/></div>
							<div class="sfField">
								<input type="text" name="network_address" value="${ifn:cleanHtmlAttribute(param.network_address)}">
								<input type="hidden" name="network_address@op" value="ico" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel"><insta:ltext key="generalmasters.dialysismachinemasters.list.status"/></div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="A,T,I" optexts="${status}" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
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

	  	<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
	  				<insta:sortablecolumn name="machine_name" title="${machinename}"/>
	  				<th><insta:ltext key="generalmasters.dialysismachinemasters.list.type"/></th>
	  				<insta:sortablecolumn name="location_name" title="${locationname}"/>
	  				<th><insta:ltext key="generalmasters.dialysismachinemasters.list.networkaddress"/></th>
	  				<th><insta:ltext key="generalmasters.dialysismachinemasters.list.remarks"/></th>
	  			</tr>
	  			<c:forEach var="list" items="${pagedList.dtoList}" varStatus="st">

	  				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{machine_id: '${list.machine_id}', location_id: '${list.location_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1)*pagedList.pageSize+st.index+1 }</td>
	  					<td>
							<c:if test="${list.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${list.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							<c:if test="${list.status eq 'T'}"><img src='${cpath}/images/blue_flag.gif'></c:if>
							${list.machine_name}
						</td>
						<td>${list.machine_type}</td>
	  					<td>${list.location_name}</td>
	  					<td>${list.network_address}</td>
	  					<td>${list.remarks}</td>
	  				</tr>

	  			</c:forEach>
	  		</table>
	  		<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
	  	</div>

		 	<c:url var="url" value="DialMachMaster.do">
		 		<c:param name="_method" value="add"/>
		 	</c:url>

<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />"><insta:ltext key="generalmasters.dialysismachinemasters.list.addnewmachine"/></a></div>

		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
			<div class="flagText"><insta:ltext key="generalmasters.dialysismachinemasters.list.temporarilydown"/></div>
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText"><insta:ltext key="generalmasters.dialysismachinemasters.list.inactive"/></div>
		</div>

	</form>

	</body>
</html>
