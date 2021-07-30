<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Scheduled Export Preference List - Insta HMS</title>
	<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		var toolbar = {
			Edit : {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/scheduledexportprefs/ScheduledExportPrefs.do?_method=show',
				description: "Edit Schedule Preferences"
			}
		}
		function init() {
			createToolbar(toolbar);
		}
		function checkDeletedItems() {
			var checkBoxes = document.getElementsByName("_deletePrefs");
			var anyChecked = false;
			var anyDisabled = false;
			for (var i=0; i<checkBoxes.length; i++) {
				if (checkBoxes[i].checked) {
					anyChecked = true;
					break;
				}
			}
			if (!anyChecked) {
				alert("Check one or more Preferences to delete");
				return false;
			}
		}
	</script>
</head>
<body onload="init()">
	<c:set var="dtoList" value="${pagedList.dtoList}"/>
	<c:set var="results" value="${not empty pagedList.dtoList}"/>
	<h1>Scheduled Export Preferences List</h1>
	<insta:feedback-panel/>
	<form name="searchForm" action="ScheduledExportPrefs.do">
		<input type="hidden" name="_method" value="list">
		<insta:search-lessoptions form="searchForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Period</div>
						<div class="sboFieldInput">
							<select name="period" id="period" class="dropdown">
								<option value="">-- Select --</option>
								<option value="H">Hourly</option>
								<option value="D">Daily</option>
								<option value="W">Weekly</option>
							</select>
						</div>
					</td>
					<td class="sboField" style="height:70px">
						<div class="sboFieldLabel">Schedule Name: </div>
						<div class="sboFieldInput">
							<input type="text" name="schedule_name" value="${ifn:cleanHtmlAttribute(param.schedule_name)}"/>
						</div>
					</td>
				</tr>
			  </table>
		</insta:search-lessoptions>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<form name="prefsForm" action="ScheduledExportPrefs.do" method="POST">
		<input type="hidden" name="_method" value="delete"/>
		<div class="resultList" >
			<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
				<tr>
					<th style="padding-top: 0px;padding-bottom: 0px">
						<input type="checkbox" name="_checkAllForDelete" onclick="return checkOrUncheckAll('_deletePrefs', this)"/>
					</th>
					<insta:sortablecolumn name="schedule_name" title="Schedule Name"/>
					<th>Period</th>
					<th>Time</th>
					<th>Minutes</th>
					<th>Account Group</th>
				</tr>
				<c:forEach items="${dtoList}" var="bean" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
								{schedule_id:'${bean.map.schedule_id}'},'');" id="toolbarRow${st.index}">
						<td><input type="checkbox" name="_deletePrefs" value="${bean.map.schedule_id}" /></td>
						<td>${bean.map.schedule_name}</td>
						<td>${bean.map.period}</td>
						<td>
							<c:if test="${bean.map.period != 'H'}">
								<fmt:formatDate pattern="HH:mm" value="${bean.map.daily_or_weekly_time}"/>
							</c:if>
						</td>
						<td>${bean.map.hourly_time}</td>
						<td>${bean.map.account_group_name}</td>
					</tr>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${results}"/>
		</div>
		<div class="fltL" style="width: 50%; margin-top: 5px; display: ${results?'block':'none'}">
			<button type="submit" name="delete" accesskey="D"  class="button" onclick="return checkDeletedItems()">
				<b><u>D</u></b>elete</button>&nbsp;
		</div>
		<table style="margin-top: 10px;float: left">
			<tr>
				<td><a href="ScheduledExportPrefs.do?_method=add">Add Prefs</a></td>
			</tr>
		</table>
	</form>
</body>
</html>