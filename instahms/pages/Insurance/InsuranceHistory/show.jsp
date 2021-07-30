<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Insurance - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="/Insurance/insurance.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<style type="text/css">
		.Doc_R { background-color: #EAD6BB;}
		table.legend {border-collapse : collapse ; margin-left : 6px }
		table.legend td {border : 1px solid grey ;padding 2px 5px }
		table.search td { white-space: nowrap }
	</style>
	<script>
		var toolbar = {
			ViewMessage: {
				title: "View Message",
				imageSrc: "icons/View.png",
				href: 'Insurance/InsuranceViewMessage.do?_method=show',
				onclick: null,
				description: "View Message"
			},
			ViewDoc: {
				title: "View Document",
				imageSrc: "icons/Report.png",
				href: 'Insurance/InsuranceViewMessage.do?_method=getDocument',
				onclick: null,
				description: "View Uploaded Document"
			}
		};

		function initHistory(){
			createToolbar(toolbar);
		}
	</script>
</head>

<body onload="initHistory();">
<c:set var="hasResults" value="${not empty HistoryList}"/>
<div class="pageHeader">Transaction History</div>
<insta:feedback-panel/>
<c:choose>
	<c:when test="${not empty visit_id}">
		<insta:patientdetails  visitid="${visit_id}" />
	</c:when>
	<c:otherwise>
		<insta:patientgeneraldetails  mrno="${mr_no}" />
	</c:otherwise>
</c:choose>

<form name="history" action="InsuranceHistory.do" method="POST">
<input type="hidden" name="_method" value="show"/>
<input type="hidden" name="insurance_id" value="${ifn:cleanHtmlAttribute(insurance_id)}">
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(mr_no)}">
<input type="hidden" name="visit_id" value="${ifn:cleanHtmlAttribute(visit_id)}">

<div class="resultList">
	<table class="resultList" cellpadding="0" cellspacing="0" id="resultTable" onmouseover="hideToolBar('');"  >
		<tr onmouseover="hideToolBar();">
			<th>X</th>
			<th>Date</th>
			<th>User</th>
			<th>Description/Subject</th>
			<th>Documents</th>
		</tr>
		<c:set var="tid" value="" />
		<c:forEach items="${HistoryList}" var="pdocument" varStatus="st">
			<c:set var="viewMessageEnabled" >${pdocument.map.doc_type == 'S'}</c:set>
			<c:set var="viewDocEnabled" >${pdocument.map.doc_type == 'R'}</c:set>

			<c:set var="flagColor">
				<c:choose>
					<c:when test="${pdocument.map.doc_type == 'R'}">yellow</c:when>
					<c:otherwise>empty</c:otherwise>
				</c:choose>
			</c:set>
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{mr_no: '${ifn:cleanJavaScript(mr_no)}', visit_id: '${ifn:cleanJavaScript(visit_id)}',insurance_id: '${ifn:cleanJavaScript(insurance_id)}',transaction_id: '${pdocument.map.docid}',
						  attachment_id: '${pdocument.map.attachment_id}' , doc_type: '${pdocument.map.doc_type}' },
						[${viewMessageEnabled},${viewDocEnabled}]);" onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

				<c:if test="${pdocument.map.doc_type eq  'R'}">
					<td>
						<input type="checkbox" name="deleteRecdDoc" id="deleteRecdDoc" value="${pdocument.map.docid}">
					</td>
				</c:if>
				<c:if test="${pdocument.map.doc_type eq  'S'}">
					<td></td>
				</c:if>
					<td><fmt:formatDate pattern="dd-MM-yyyy" value="${pdocument.map.datetime}"/></td>
					<td>${pdocument.map.user_id}</td>
					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${pdocument.map.email_subject}</td>
				<c:if test="${pdocument.map.doc_type eq  'R'}">
					<td>${pdocument.map.doc_title}</td>
				</c:if>
				<c:if test="${pdocument.map.doc_type eq  'S'}">
					<td>
						<c:forEach items="${AttacchmentsList}" var="attachments" varStatus="st">
							<c:if test="${pdocument.map.docid == attachments.map.docid}">
								${attachments.map.doc_title}</br>
							</c:if>
						</c:forEach>
					</td>
				</c:if>
			</tr>
		</c:forEach>
	</table>
</div>
<c:if test="${!hasResults}">
	<div style="width: 951px; height: 35px; border: 1px solid #E0E0E0; border-top: none; background-color:#FFC">
		<div style="float: left; width: 25px; margin-top: 10px; margin-left: 3px;">
			<img src="${cpath}/images/alert.png"/>
		</div>
		<div style="float: left; margin-top: 10px">
			${not empty message ? message : 'No Transactions Available'}
		</div>
	</div>
</c:if>

<div class="screenActions">
	<button type="button" name="delete" id="delete" accesskey="D" value="Delete" onclick="fundeleteRecdDocs()"><b><u>D</u></b>elete</button> |
	<a href="InsuranceDashboard.do?_method=list&filterClosed=true&status=A&status=P&status=F&sortOrder=insurance_id&sortReverse=true" >Case List</a> |
	<c:choose>
		<c:when test="${not empty visit_id}">
			<a href="AddOrEditCase.do?_method=addshow&insurance_id=${ifn:cleanURL(insurance_id)}&visit_id=${ifn:cleanURL(visit_id)}">Case</a> |
			<a href="UploadReceivedDocs.do?_method=add&visit_id=${ifn:cleanURL(visit_id)}&insurance_id=${ifn:cleanURL(insurance_id)}&mr_no=${ifn:cleanURL(mr_no)}">Upload</a> |
		</c:when>
		<c:otherwise>
			<a href="AddOrEditCase.do?_method=addshow&insurance_id=${ifn:cleanURL(insurance_id)}">Case</a> |
			<a href="UploadReceivedDocs.do?_method=add&mr_no=${ifn:cleanURL(mr_no)}&insurance_id=${ifn:cleanURL(insurance_id)}">Upload</a> |
		</c:otherwise>
	</c:choose>
	<a href="SendToTpa.do?_method=show&mr_no=${ifn:cleanURL(mr_no)}&visit_id=${ifn:cleanURL(visit_id)}&insurance_id=${ifn:cleanURL(insurance_id)}">Send</a>
</div>
<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
	<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
	<div class="flagText"> Documents Uploaded</div>
	<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
	<div class="flagText"> Documents Sent</div>
</div>
</form>
</body>
</html>
