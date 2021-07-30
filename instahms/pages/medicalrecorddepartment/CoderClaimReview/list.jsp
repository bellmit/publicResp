<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Coder Claim Reviews - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardLookup.js"/>
	<c:set var="pagePath" value="<%=URLRoute.CODER_CLAIM_REVIEW %>"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
		input[type=text]{text-transform:uppercase};
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}coderreviews/show.htm?',
				onclick: null,
				description: "View review details"
				}
		};
		isDoctor = "${isDoctor}";
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.CoderReviewSearchForm);
			//convert to uppercase
			$('form[name=CoderReviewSearchForm]').submit(function(){
			    var patient_id = $('input[name=patient_id]').val();
			    if( patient_id.length > 0 ) {
					$('input[name=patient_id]').val(patient_id.toUpperCase());
			    }
			    var mrno = $('input[name=mr_no]').val();
			    if( mrno.length > 0 ) {
					$('input[name=mr_no]').val(mrno.toUpperCase());
			    }
			});
			$('#checkbox_created_by').click(function(){
				if( $(this).prop("checked") ){
					$('#checkbox_assignedto').prop("checked",false);
					$('#checkbox_assigned_to_role_name').prop("checked",false);
					$('#checkbox_all').prop("checked",false);
				} 
			});
			$('#checkbox_assignedto').click(function(){
				if( $(this).prop("checked") ){
					$('#checkbox_created_by').prop("checked",false);
					$('#checkbox_assigned_to_role_name').prop("checked",false);
					$('#checkbox_all').prop("checked",false);
				} 
			});
			$('#checkbox_assigned_to_role_name').click(function(){
				if( $(this).prop("checked") ){
					$('#checkbox_created_by').prop("checked",false);
					$('#checkbox_assignedto').prop("checked",false);
					$('#checkbox_all').prop("checked",false);
				} 
			});
			$('#checkbox_all').click(function(){
				if( $(this).prop("checked") ){
					$('#checkbox_created_by').prop("checked",false);
					$('#checkbox_assignedto').prop("checked",false);
					$('#checkbox_assigned_to_role_name').prop("checked",false);
				} 
			});
			if( isDoctor  == "true"){ 
				$("#checkbox_all, label[for=checkbox_all],#checkbox_created_by,label[for=checkbox_created_by],label[for=checkbox_assigned_to_role_name],#checkbox_assigned_to_role_name").remove();
				$("#checkbox_assignedto").prop("checked",true);
			}
		}
		
	</script>
</head>
<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Coder Claim Reviews</h1>
	<insta:feedback-panel/>

	<form name="CoderReviewSearchForm" method="GET">
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:find form="CoderReviewSearchForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Mr No:</div>
					<div class="sboFieldInput">
						<input type="text"  name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel">Visit Id:</div>
					<div class="sboFieldInput">
						<input type="text"  name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}">
					</div>
				</div>
			</div>
			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td class="last">
							<div class="sfLabel">Review Status</div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="open,inprogress,closed-resolved,closed-unresolved" optexts="Open,Inprogress, Closed-resolved, Closed-unresolved" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
								<input type="hidden" name="status@type" value="text" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Review</div>
							<div class="sfField">
								<input type="checkbox" name="created_by" value="${loggedInUserIdStr}" id="checkbox_created_by" <c:if test="${not empty(ifn:cleanHtmlAttribute(param.created_by)) or reviewTypeFilter == 'created_by'}"> checked="checked" </c:if> /><label for="checkbox_created_by">Created by me</label><br />
								<input type="checkbox" name="assignedto" value="${loggedInUserIdStr}" id="checkbox_assignedto" <c:if test="${not empty(ifn:cleanHtmlAttribute(param.assignedto)) or reviewTypeFilter == 'assignedto'}"> checked="checked" </c:if> /><label for="checkbox_assignedto">Assigned to me</label><br />
								<input type="checkbox" name="assigned_to_role" value="${loggedInRoleId}" id="checkbox_assigned_to_role_name" <c:if test="${not empty(ifn:cleanHtmlAttribute(param.assigned_to_role)) or reviewTypeFilter == 'assigned_to_role'}"> checked="checked" </c:if> /><label for="checkbox_assigned_to_role_name">Assigned to role</label> 
								<input type="hidden" name="assigned_to_role@cast" value="y" /><br />
								<input type="checkbox" name="review_type_id" value="0" id="checkbox_all" <c:if test="${not empty(ifn:cleanHtmlAttribute(param.review_type_id)) or reviewTypeFilter == 'all'}"> checked="checked" </c:if> /><label for="checkbox_all">All</label><br />
								<input type="hidden" name="review_type_id@op" value="nin" />
								<input type="hidden" name="review_type_id@type" value="integer" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Review Created Date</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="registration.patient.adt.from"/>:</div>
								<insta:datewidget name="created_at" valid="past"	id="created_start_date" value="${(not empty paramValues.created_at )?paramValues.created_at[0]:created_at[0]}" />
								<input type="hidden" name="created_at@cast" value="y">
								<input type="hidden" name="created_at@type" value="date">
							</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="registration.patient.adt.to"/>:</div>
								<insta:datewidget name="created_at" valid="past"	id="created_end_date" value="${paramValues.created_at[1]}" />
								<input type="hidden" name="created_at@op" value="ge,le">
								<input type="hidden" name="created_at@cast" value="y">
								<input type="hidden" name="created_at@type" value="date">
							</div>
						</td>
						<td class="last">&nbsp;</td>
					</tr>
				</table>
			</div>
		</insta:find>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="mr_no" title="MRN"/>
					<insta:sortablecolumn name="patient_id" title="Visit Id"/>
					<insta:sortablecolumn name="patient_name" title="Patient Name"/>
					<insta:sortablecolumn name="title" title="Reason"/>
					<insta:sortablecolumn name="created_by_fullname" title="Created By"/>
					<insta:sortablecolumn name="created_at" title="Created At"/>
					<insta:sortablecolumn name="assigned_to_role_name" title="Assigned Role"/>
					<insta:sortablecolumn name="assignedToName" title="Assigned To"/>
					<insta:sortablecolumn name="status" title="Status"/>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{id: '${record.ticket_id}',patient_id:'${record.patient_id }'},'');" id="toolbarRow${st.index}">
						<td>
							${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}
						</td>
						<td>${record.mr_no}</td>
						<td>${record.patient_id}</td>
						<td><insta:truncLabel value="${record.patient_name}" length="25"/></td>
						<td><insta:truncLabel value="${record.title}" length="25"/></td>
						<td><insta:truncLabel value="${record.created_by_fullname}" length="35"/></td>
						<td>
							<fmt:formatDate value="${record.created_at }" pattern="yyyy-MM-dd kk:mm" />
						</td>
						<td><insta:truncLabel value="${record.assigned_to_role_name}" length="15" /></td>
						<td><insta:truncLabel value="${record.assignedtoname}" length="15"/></td>
						<td>${record.status}</td>
					</tr>
				</c:forEach>
			</table>
				<insta:noresults hasResults="${hasResults}"/>
		</div>
		<div class="screenActions" style="float:left">
			<c:if test="${ not empty urlRightsMap.update_mrd and urlRightsMap.update_mrd == 'A' && ( not empty codificationStatus  && ( codificationStatus == 'P' || codificationStatus == 'C' )) }" >
				<c:url var="url" value="${pagePath}coderreviews/add.htm?patient_id=${ifn:cleanHtmlAttribute(param.patient_id)}&mr_no=${ifn:cleanHtmlAttribute(param.mr_no)}"></c:url>
				<c:if test="${ifn:cleanHtmlAttribute(param.patient_id) ne null and ifn:cleanHtmlAttribute(param.patient_id) ne '' }">
					<a href="<c:out value='${url}' />">Add Review</a> <label>|</label>
				</c:if>
			</c:if>
			<c:if test="${ not empty param.patient_id  and not empty codificationStatus }">
				<c:url var="coderScreenUrl" value="/pages/medicalrecorddepartment/MRDUpdate.do?_method=getMRDUpdateScreen&patient_id=${ifn:cleanHtmlAttribute(param.patient_id)}"></c:url>
				<a href="<c:out value='${coderScreenUrl}' />">Codification Screen</a>
			</c:if>
		</div>

	</form>
</body>
</html>
