<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<html>

<head>
<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
<script>
		var toolbarOptions = getToolbarBundle("js.dialysismodule.commonvalidations.toolbar");
</script>
	<title><insta:ltext key="patient.dialysis.sessions.screen"/></title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>

	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dialysis/dialysissessions.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<style type="text/css">
		.alert { background-color: 	#EAD6BB }
	</style>

	<style type="text/css">
	</style>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
</head>
<c:set var="sesList" value="${pagedList.dtoList}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty sesList}"/>
<c:set var="mrno">
 <insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="machineid">
 <insta:ltext key="patient.dialysis.sessions.machine"/>
</c:set>
<c:set var="attendant">
 <insta:ltext key="patient.dialysis.sessions.attendant"/>
</c:set>
<c:set var="status">
 <insta:ltext key="patient.dialysis.sessions.status"/>
</c:set>
<c:set var="machinests">
 <insta:ltext key="patient.dialysis.sessions.machinests"/>
</c:set>
<c:set var="start">
 <insta:ltext key="patient.dialysis.sessions.start"/>
</c:set>
<c:set var="allvalue">
	<insta:ltext key="patient.dialysis.common.all"/>
</c:set>

<c:set var="OrderedOptions">
 <insta:ltext key="patient.dialysis.sessions.Ordered"/>,
 <insta:ltext key="patient.dialysis.sessions.prepared"/>,
 <insta:ltext key="patient.dialysis.sessions.inprogress"/>,
 <insta:ltext key="patient.dialysis.sessions.completed"/>
</c:set>

<body onload="init();">
<div class="pageHeader"><insta:ltext key="patient.dialysis.sessions.pageHeader"/></div>
<insta:feedback-panel/>
<form name="dialysisSessionsListForm" method="get" action="${cpath}/dialysis/DialysisCurrentSessions.do">
<input type="hidden" name="_method" value="list"/>
<input type="hidden" name="_searchMethod" value="list"/>
	<insta:search form="dialysisSessionsListForm" optionsId="optionalFilter" closed="${hasResults}" >
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="patient.dialysis.sessions.machine"/>:</div>
				<div class="sboFieldInput">
					<select name="machine_id" id="machine_id" class="dropdown">
						<option value=""><insta:ltext key="patient.dialysis.sessions.optionvalue"/></option>
						<c:forEach items="${machines}" var="machine">
							<option value="${machine.map.machine_id}"
								${machine.map.machine_id == param.machine_id ? 'selected' : ''}>${machine.map.machine_name}</option>
						</c:forEach>
					</select>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable" width="100%" align="center" >
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="patient.dialysis.sessions.staff"/></div>
						<div class="sfField">
							<insta:selectdb name="start_attendant" table="u_user" valuecol="emp_username"
									displaycol="emp_username" filtered="false"  value="${paramValues.start_attendant}" dummyvalue="${allvalue}" />
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="patient.dialysis.sessions.start.date"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="patient.dialysis.sessions.from"/></div>
							<insta:datewidget name="start_time0" id="start_time0" valid="past" value="${param.start_time0}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="patient.dialysis.sessions.to"/></div>
							<insta:datewidget name="start_time1" id="start_time1" valid="past" value="${param.start_time1}"/>
							<input type="hidden" name="start_time@op" value="ge,le"/>
							<input type="hidden" name="start_time@type" value="date"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="patient.dialysis.sessions.status"/>:</div>
						<div class="sfField">
						<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="O,P,I,F" optexts="${OrderedOptions}"/>
						</div>
					</td>
					<td class="last">
                         <div class="sfLabel"><insta:ltext key="patient.dialysis.sessions.odered.date"/></div>
                         <div class="sfField">
                               <div class="sfFieldSub"><insta:ltext key="patient.dialysis.sessions.from"/></div>
                               <insta:datewidget name="order_time0" id="order_time0" valid="past" value="${param.order_time0}"/>
                               </div>
                         <div class="sfField">
                              <div class="sfFieldSub"><insta:ltext key="patient.dialysis.sessions.to"/></div>
                              <insta:datewidget name="order_time1" id="order_time1" valid="past" value="${param.order_time1}"/>
                              <input type="hidden" name="order_time@op" value="ge,le"/>
                              <input type="hidden" name="order_time@type" value="date"/>
                         </div>
                    </td>
					<td class="last"></td>
				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<table class="resultList" cellpadding="0" cellspacing="0" id="resultTable" onmouseover="hideToolBar('');" >
		<tr onmouseover="hideToolBar();">
			<th><insta:ltext key="patient.dialysis.sessions.sl.no"/></th>
			<insta:sortablecolumn name="mr_no" title="${mrno}"/>
			<th><insta:ltext key="patient.dialysis.sessions.patient"/></th>
			<th><insta:ltext key="patient.dialysis.sessions.ordered.date"/></th>
			<insta:sortablecolumn name="machine_id" title="${machineid}"/>
			<insta:sortablecolumn name="start_attendant" title="${attendant}"/>
			<th><insta:ltext key="patient.dialysis.sessions.alert"/></th>
			<insta:sortablecolumn name="status" title="${status}"/>
			<insta:sortablecolumn name="machine_status" title="${machinests}"/>
			<insta:sortablecolumn name="start_time" title="${start}"/>
		</tr>
		<c:forEach var="ses" items="${sesList}" varStatus="st">
			<c:set var="flagColor">
			<c:choose>
				<c:when test="${ses.alerts >= 1}">red</c:when>
				<c:when test="${ses.bill_type == 'P' && ses.bill_status != 'C'}">blue</c:when>
				<c:otherwise>empty</c:otherwise>
			</c:choose>
			</c:set>
			<c:set var="billPaid">
				<c:choose>
					<c:when test="${ses.bill_type == 'P' && ses.bill_status == 'C'}"><insta:ltext key="patient.dialysis.sessions.true"/></c:when>
					<c:when test="${ses.bill_type == 'C'}"><insta:ltext key="patient.dialysis.sessions.true"/></c:when>
					<c:otherwise><insta:ltext key="patient.dialysis.sessions.false"/></c:otherwise>
				</c:choose>
			</c:set>
			<tr  class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{mr_no: '${ses.mr_no}', order_id: '${ses.order_id}',prescriptionId: '${ses.prescription_id}',
								visit_center: '${ses.visit_center}'}, [${billPaid},null,null,null]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
				<c:set var="statusName" value="${ses.status!=null ? ses.status_name : 'Not Initiated'}"/>
				<td>${((pagedList.pageNumber -1)*pagedList.pageSize) +st.index+1}</td>
				<td>${ses.mr_no}</td>
				<td><insta:truncLabel value="${ses.patient_name}" length="30" /></td>
				<td><fmt:formatDate value="${ses.ordered_date}" pattern="dd-MM-yyyy HH:mm"/></td>
				<td>${ses.machine_name}</td>
				<td>${ses.start_attendant}</td>
				<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${ses.alerts}</td>
				<td>${statusName}</td>
				<td>${ses.machine_status}</td>
				<td><fmt:formatDate value="${ses.start_time}" pattern="dd-MM-yyyy HH:mm"/></td>
			</tr>
		</c:forEach>
	</table>
	<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.dialysis.sessions.alert"/></div>
		<div class="flag"><img src="${cpath}/images/blue_flag.gif"></div>
		<div class="flagText"><insta:ltext key="patient.dialysis.sessions.unpaid.bill"/></div>
	</div>
</form>
</body>
</html>