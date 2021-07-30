<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<title>Favourite Reports</title>
<insta:link type="css" file="hmsNew.css" />
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="widgets.js" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="script" file="/reports/favourite_reports_dashboard.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<style>
	#xDialog_mask.mask {
		    z-index: 1;
		    display: none;
		    position: absolute;
		    top: 0;
		    left: 0;
		    -moz-opacity: 0.0001;
		    opacity: 0.0001;
		    filter: alpha(opacity=50);
		    background-color: #CCC;
	}
</style>
<script>
	var isAdmin = '${roleId == 1 || roleId == 2}';
	var urlList = ${urlListJSON};
	var paramList = ${paramListJSON};



function stopToolBarRightClick(chk) {
	var e=window.event||arguments.callee.caller.arguments[0];
	if (e.stopPropagation) e.stopPropagation();
	return false;
}


function clickIE() {
	if (document.all) {
		return false;
	}
}
function clickNS(e) {
	if(document.layers||(document.getElementById&&!document.all)) {
		if (e.which==2||e.which==3) {
		return false;
		}
	}
}

if (document.layers) {
	document.captureEvents(Event.MOUSEDOWN);
	document.onmousedown=clickNS;
} else{
		document.onmouseup=clickNS;
		document.oncontextmenu=clickIE;
}
document.oncontextmenu=new Function("return false")

</script>
</head>
<c:set var="req" value="${pageContext.request}" />
<c:set var="uri" value="${request.requestURI}" />

<body onload="init(); showFilterActive(document.inputform);" class="yui-skin-sam">
<form name="inputform" action="FavouriteReportsDashboard.do">
	<input type="hidden" name="_method" value="getReport">
	<input type="hidden" name="_reportsToBeDeleted" id="_reportsToBeDeleted" value=""/>
	<input type="hidden" name="_reportsToBeMarkdFreq" id="_reportsToBeMarkdFreq" value=""/>
	<input type="hidden" name="delTitle" value="">
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}" />
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}" />
	<input type="hidden" name="_freqStatus" value="N" />
	<input type="hidden" name="_recordsCount" id="_recordsCount" value="${_recordsCount}" />
	<input	type="hidden" name="_pgNm" id="_pgNm" value="${list.pageNumber}" />
	<input type="hidden" name="_loggedUser" id="_loggedUser" value="<%=com.insta.hms.common.Encoder.cleanHtmlAttribute((String) session.getAttribute("userid")) %>" />
<c:set	var="cpath" value="${pageContext.request.contextPath}" />
<div class="pageHeader">Favourite Reports</div>
	<insta:feedback-panel />

	<%-- NOTE: the "method" parameter name has to be _method for Save Searches to work correctly --%>

	<%-- NOTE: _searchMethod is used by the Save Search functionality.
		Set this to the method that is to be called for doing the search --%>
	<input type="hidden" name="_searchMethod" value="getReport"/>
<c:set var="repList" value="${list.dtoList}" />
	<c:set var="hasResults" value="${not empty repList}"/>
	<insta:search form="inputform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sfLabel">Report Group </div>
				<c:set var="emergentScreen" value="${param._emergentScreen == null ? (_emergentScreen == null? actionId:_emergentScreen): param._emergentScreen}" />
				<input type="hidden" name="_emergentScreen" value="${emergentScreen}" />
				<input type="hidden" name="_actionId" value="${actionId}" />

				<c:if test="${actionId eq  'fav_rpt_dashbd' && (emergentScreen == 'fav_rpt_dashbd')}">
				<div class="sfField">
					<select name="report_group" id="report_group" value="${param.report_group}" class="dropDown">
						<option value="">(All)</option>
						<option value="Billing Reports" ${param.report_group == 'Billing Reports' ? 'selected' : ''}>Billing Reports</option>
						<option value="Consultation Reports" ${param.report_group == 'Consultation Reports' ? 'selected' : ''}>Consultation Reports</option>
						<option value="Dialysis Reports" ${param.report_group == 'Dialysis Reports' ? 'selected' : ''}>Dialysis Reports</option>
						<option value="Diagnostics Reports" ${param.report_group == 'Diagnostics Reports' ? 'selected' : ''}>Diagnostics Reports</option>
						<option value="Financial Reports" ${param.report_group == 'Financial Reports' ? 'selected' : ''}>Financial Reports</option>
						<option value="Insurance Reports" ${param.report_group == 'Insurance Reports' ? 'selected' : ''}>Insurance Reports</option>
						<option value="Misc. Reports" ${param.report_group == 'Misc. Reports' ? 'selected' : ''}>Misc. Reports</option>
						<option value="Patient Stats Reports" ${param.report_group == 'Patient Stats Reports' ? 'selected' : ''}>Patient Stats Reports</option>
						<option value="Procurement Reports" ${param.report_group == 'Procurement Reports' ? 'selected' : ''}>Procurement Reports</option>
						<option value="Sales/Issues Reports" ${ram.report_group == 'Sales/Issues' ? 'selected' : ''}>Sales/Issues Reports</option>
						<option value="Store Mgmt Reports" ${param.report_group == 'Store Mgmt Reports' ? 'selected' : ''}>Store Mgmt Reports</option>
						<option value="Stores Reports" ${param.report_group == 'Stores Reports' ? 'selected' : ''}>Stores Reports</option>
						<option value="Network Reports" ${param.report_group == 'Network Reports' ? 'selected' : ''}>Network Reports</option>
					</select>
				</div>
				</c:if>
				<c:if test="${actionId ne  'fav_rpt_dashbd'  || (emergentScreen ne 'fav_rpt_dashbd')}">
					<div class="sfField">
					${param.report_group}
					<input type="hidden" name="report_group" id="report_group" value="${param.report_group}">
					<input type="hidden" name="_actionId" value="${actionId}" />
					</div>
				</c:if>
			</div>
			<div class="sboField">
				<div class="sfLabel">Status</div>
				<div class="sfField">
					<insta:checkgroup name="frequently_viewed" selValues="${paramValues.frequently_viewed}"
					opvalues="Y,N" optexts="Frequently Viewed,Others"/>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${not empty repList? 'none' : 'block'}" >
			<table class="searchFormTable" >
				<tr>
					<td>
						<div class="sfLabel">Report Name</div>
						<div class="sfField">
							<input type="text" name="report_title" value="${ifn:cleanHtmlAttribute(param.report_title)}"/>
							<input type="hidden" name="report_title@op" value="ilike" />
						</div>
					</td>
					<td>
						<div class="sfLabel">Report Type</div>
						<div class="sfField">
							<select name="parent_report_name" id="parent_report_name" value="${ifn:cleanHtmlAttribute(param.parent_report_name)}" class="dropDown">
								<option value="">--(All)--</option>
								<c:forEach var="rType" items="${typeLst}" >
								<c:set var="selected" value="${rType eq param.parent_report_name ?'selected':''}"/>
								<option value= "${rType}" ${selected} >${rType}</option></c:forEach>
							</select>
						</div>
					</td>
					<td>
						<div class="sfLabel">Created Date</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="created_date" id="created_date0" value="${paramValues.created_date[0]}"/>
							<input type="hidden" name="created_date@op" value="ge"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="created_date" id="created_date1" value="${paramValues.created_date[1]}"/>
							<input type="hidden" name="created_date@op" value="le"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Created By</div>
						<div class="sfField">
							<select name="user_name" id="user_name" value="${ifn:cleanHtmlAttribute(param.user_name)}" class="dropDown">
								<option value="">--(All)--</option>
								<c:forEach var="rCreator" items="${creatorLst}" >
								<c:set var="selected" value="${rCreator eq param.user_name ?'selected':''}"/>
								<option value= "${rCreator}" ${selected}>${rCreator}</option></c:forEach>
							</select>
						</div>
					</td>
					<td class="last"></td>
				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${list.pageNumber}" numPages="${list.numPages}" totalRecords="${list.totalRecords}"/>
	<div style="width:952px;overflow-x: auto; overflow-y: hidden;">
		<table class="resultList" align="center" id="dragTable" >
			<tr>
				<th> <input type="checkbox" name="selAllChkbx" id="selAllChkbx" value="" onclick="selectAllForDiscounts();"/></th>
				<th>#</th>
				<th>
					<insta:sortablecolumn name="report_title" add_th="false"
						title="Report Name" />
				</th>
				<th>
					Period
				</th>
				<th>
					<insta:sortablecolumn name="parent_report_name"
					add_th="false" title="Report Type" />
				</th>
				<th>
					<insta:sortablecolumn name="report_group" add_th="false"
					title="Report Group" />
				</th>
				<th title="Frequently Viewd">
					<insta:sortablecolumn name="frequently_viewed"
					add_th="false" title="Freq." />
				</th>
				<th>
					<insta:sortablecolumn name="user_name" add_th="false"
					title="Created By" />
				</th>
				<th>
					<insta:sortablecolumn name="created_date" add_th="false"
						title="Created Date" />
				</th>
			</tr>

			<c:forEach var="record" items="${repList}" varStatus="st">
			<c:set var="runRightsAvlb" value="${runRightsList[st.index] eq 'Y'? 'true': 'false'}" />
			<c:set var="creatorRightsAvlb" value="${creatorRightsList[st.index] eq 'Y'? 'true': 'false'}" />
			<c:set var="reportRightsAvlb" value="${reportRightsList[st.index] eq 'Y'? 'true': 'false'}" />


			<c:set var="urlRun" value="/pages/Reports/FavouriteReportsDashboard.do" />
				<tr onclick="showToolbar(${st.index}, event, 'dragTable',
							{'%url': '<%= ((String)request.getAttribute("javax.servlet.forward.request_uri")).substring(((String)request.getAttribute("javax.servlet.forward.request_uri")).indexOf("/pages/"))%>'
							, '%params' : '${record.report_id}'
							 , _savedfavourite : '${record.report_title}',_myreport:'${record.report_id}',
							 _actionId:'${record.action_id}'},
							[${reportRightsAvlb},${creatorRightsAvlb},${runRightsAvlb}]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td>
						<input type="checkbox" name="_chkbx" id="_${record.report_title}"
							value="${record.report_title}" onclick="checkifselAllChkbxChkd()"/>
					</td>
					<td>${(list.pageNumber-1) * list.pageSize + st.index + 1 }${rightsList[st.index]}</td>
					<td><insta:truncLabel value="${record.report_title}" length="30"/></td>
					<td>${periodList[st.index]}</td>
					<td><insta:truncLabel value="${record.parent_report_name}" length="30"/></td>
					<td><insta:truncLabel value="${record.report_group}" length="30"/></td>
					<td align="center">
						<c:choose>
							<c:when test="${record.frequently_viewed eq 'N'}">
								<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
								No
							</c:when>
							<c:otherwise>
								<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
								Yes
							</c:otherwise>
						</c:choose>
					</td>
					<td>${record.user_name}
						<input type="hidden" id="${record.report_title}_User" value="${record.user_name}"/>
					</td>
					<td><fmt:formatDate value="${record.created_date}" pattern="dd-MM-yyyy"/></td>
				</tr>
			</c:forEach>
		</table>
	</div>
<input type="hidden" name="_emergentScreen" value="${emergentScreen}" />
<insta:noresults hasResults="${not empty repList}"
	message="No reports were found for the given search criteria." />
<div class="screenActions">
	<table align="left">
		<tr>
			<td><input type="button" name="delete_button" value="Delete" onclick="onDelete();"/> &nbsp;</td>
			<td><input type="button" name="freq_button" value="Toggle Frequently Viewed" onclick="return onToggleFrequentlyViewed();"/></td>
		</tr>
	</table>
</div>

<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
	<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
	<div class="flagText">Frequently Viewed</div>
	<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
	<div class="flagText">Others</div>
</div>

</form>
</body>
</html>

