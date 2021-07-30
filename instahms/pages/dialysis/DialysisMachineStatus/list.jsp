<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title><insta:ltext key="patient.dialysis.machine.status.screen"/></title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

     <meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
     <meta name="i18nSupport" content="true"/>
	<style type="text/css">
		.cannotConnect{ background-color: #F2555C}
		.dialyzing{ background-color:  	#F3E9EA}

	</style>
	<insta:js-bundle prefix="dialysismodule.machinedetails"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.dialysismodule.machinedetails.toolbar");
	</script>
	<script>
		function clearAll() {
			var allocStatus = document.MachineStatusForm.allocStatus ;
			var machStatus = document.MachineStatusForm.machStatus ;
			for(var i=0; i< allocStatus.length; i++) {
				if(allocStatus[i].value == '') allocStatus[i].checked = true;
				else {
					allocStatus[i].checked = false;
					allocStatus[i].disabled = true;
				}
			}
			for(var i=0; i< machStatus.length; i++) {
				if(machStatus[i].value == '') machStatus[i].checked = true;
				else {
					machStatus[i].checked = false;
					machStatus[i].disabled = true;
				}
			}
		}
		var toolbar = {
			Edit: {
				title: toolbarOptions["editvisit"]["name"],
				imageSrc: "icons/Edit.png",
				href: 'dialysis/DialysisMachineStatus.do?method=machStatusScreen',
				onclick: null,
				description: toolbarOptions["editvisit"]["description"]
			},

			Session: {
				title: toolbarOptions["editvisitsession"]["name"],
				imageSrc: "icons/Edit.png",
				href: 'dialysis/DialysisCurrentSessions.do?_method=getSessionsScreen',
				onclick: null,
				description: toolbarOptions["editvisitsession"]["description"]
			}
		};
		function init(){
			createToolbar(toolbar);
		}
	</script>
</head>
<c:set var="machList" value="${pagedList.dtoList}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="hasResults" value="${not empty machList}"/>
<c:set var="allocatedOptions">
 <insta:ltext key="patient.dialysis.machine.status.allocated"/>,
 <insta:ltext key="patient.dialysis.machine.status.free"/>
</c:set>

<c:set var="dialyzingOptions">
 <insta:ltext key="patient.dialysis.machine.status.dialyzing"/>,
 <insta:ltext key="patient.dialysis.machine.status.not.dialyzing"/>,
 <insta:ltext key="patient.dialysis.machine.status.cannot.connect"/>
</c:set>


<c:set var="machinename">
 <insta:ltext key="patient.dialysis.machine.status.machinename"/>
</c:set>
<c:set var="locationname">
 <insta:ltext key="patient.dialysis.machine.status.location"/>
</c:set>
<c:set var="networkaddress">
 <insta:ltext key="patient.dialysis.machine.status.networkaddress"/>
</c:set>
<c:set var="dialysisstart">
 <insta:ltext key="patient.dialysis.machine.status.dialysisstart"/>
</c:set>
<body onload="init();">
<div class="pageHeader"><insta:ltext key="patient.dialysis.machine.status.pageHeader"/></div>
<form name="MachineStatusForm" method="GET" action="${cpath}/dialysis/DialysisMachineStatus.do">
<input type="hidden" name="method" value="list" />
	<insta:search-lessoptions form="MachineStatusForm" >
	<table class="searchBasicOpts" >
		<tr>
			<td class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="patient.dialysis.machine.status.location"/></div>
				<div class="sboFieldInput">
					<select name="locationId" id="locationId" class="dropdown">
						<option value=""><insta:ltext key="patient.dialysis.machine.status.location.value"/></option>
						<c:forEach items="${locations}" var="location">
							<option value="${location.location_id}"
								${location.location_id == param.locationId ? 'selected' : ''}>${location.location_name}</option>
						</c:forEach>
					</select>
				</div>
			</td>
			<td class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="patient.dialysis.machine.status.sboFieldLabel"/></div>
				<div class="sboFieldInput" style="height:50px">
					<insta:checkgroup name="allocStatus" opvalues="A,N" optexts="${allocatedOptions}"
						selValues="${paramValues.allocStatus}"></insta:checkgroup>
				</div>
			</td>
			<td class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="patient.dialysis.machine.status.machine.status"/></div>
				<div class="sboFieldInput" style="height:70px">
					<insta:checkgroup name="machStatus" opvalues="D,N,X" optexts="${dialyzingOptions}"
						selValues="${paramValues.machStatus}"></insta:checkgroup>
				</div>
			</td>
		</tr>
	  </table>
	  </insta:search-lessoptions>

	  <insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList" >
		  	<table class="resultList" id="resultTable" width="100%" onmouseover="hideToolBar('');">
		  		<tr onmouseover="hideToolBar();">
		  			<th>#</th>
		  			<insta:sortablecolumn name="machine_name" title="${machinename}"/>
		  			<insta:sortablecolumn name="location_name" title="${locationname}"/>
		  			<insta:sortablecolumn name="network_address" title="${networkaddress}"/>
		  			<th><insta:ltext key="patient.dialysis.machine.status.Status"/></th>
		  			<th><insta:ltext key="ui.label.mrno"/></th>
		  			<th><insta:ltext key="ui.label.patient.name"/></th>
		  			<insta:sortablecolumn name="start_time" title="${dialysisstart}"/>
		  		</tr>
		  		<c:forEach var="list" items="${machList}" varStatus="st">

			  		<c:set var="flagColor">
							<c:choose>
								<c:when test="${list.status eq 'A' && list.assigned_status eq 'A' && (list.polled_status eq 'D' || list.polled_status eq 'N')}">grey</c:when>
								<c:when test="${ list.status eq 'A' && list.assigned_status eq 'A' && (list.polled_status != 'D' || list.polled_status != 'N')}">red</c:when>
								<c:otherwise>empty</c:otherwise>
							</c:choose>
						</c:set>
			  		<c:set var="enableSession" >
			  			<c:choose>
			  				<c:when test="${empty list.order_id && empty list.prescription_id}">false</c:when>
			  				<c:otherwise>true</c:otherwise>
			  			</c:choose>
			  		</c:set>

			  		<tr  class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
								{machineID: '${list.machine_id}',order_id:'${list.order_id}',mr_no: '${list.mr_no}',prescription_id: '${list.prescription_id}'},
								[true,${enableSession},true,true]);"
							onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

			  			<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
			  			<td>${list.machine_name}</a></td>
			  			<td>${list.location_name}</td>
			  			<td>${list.network_address}</td>
			  			<c:set var="status">
			  				<c:choose>
			  					<c:when test="${list.polled_status eq 'D'}"><insta:ltext key="patient.dialysis.machine.status.dialyzing"/></c:when>
			  					<c:when test="${list.polled_status eq 'N'}"><insta:ltext key="patient.dialysis.machine.status.not.dialyzing"/></c:when>
			  					<c:when test="${list.polled_status eq 'R'}"><insta:ltext key="patient.dialysis.machine.status.not.responding"/></c:when>
			  					<c:when test="${list.polled_status eq 'E'}"><insta:ltext key="patient.dialysis.machine.status.data.error"/></c:when>
			  					<c:when test="${list.polled_status eq 'X'}"><insta:ltext key="patient.dialysis.machine.status.cannot.connect"/></c:when>
			  					<c:otherwise><insta:ltext key="patient.dialysis.machine.status.unknown"/></c:otherwise>
			  				</c:choose>
			  			</c:set>
			  			<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${ifn:cleanHtml(status)}</td>
			  			<td>${list.mr_no}</a></td>
			  			<td>${list.patient_name} ${list.last_name}</td>
			  			<td><fmt:formatDate value="${list.start_time}" pattern="dd-MM-yyyy HH:mm:ss"/></td>
			  		</tr>
			  	</c:forEach>
			</table>
		</div>

		<insta:noresults hasResults="${hasResults}"/>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.dialysis.machine.status.assigned.cannot.connect"/></div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.dialysis.machine.status.dialyzing"/></div>
		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.dialysis.machine.status.others"/></div>
	</div>
</form>
</body>
</html>
