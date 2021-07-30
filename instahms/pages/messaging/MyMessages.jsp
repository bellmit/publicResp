<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>

<head>
	<title>My Notifications - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="messaging/messaging.js"/>
	<insta:link type="css" file="widgets.css"/>
	<style type="text/css">
		.failedmsg {
			color:red;
		}
	</style>
  <script>
	contextPath = "${pageContext.request.contextPath}";
	publicPath = contextPath + "/ui/";
  </script>		
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="messageLogList" value="${pagedList.dtoList}"/>
<c:set var="hasResult" value="${not empty messageLogList}"/>
<c:set var="urlPrefix" value="${not empty messageGroup ? messageGroup : 'master'}"/>
<c:set var="searchUrl" value="${cpath}/message/MyMessages.do?_method=list" />
<c:set var="detailsUrl" value="message/MyMessages.do?_method=show" />

<c:forEach var="dispatcherEntry" items="${dispatcherMap}" varStatus="status">
	<c:set var="dispatcherValues" value="${dispatcherValues}${(status.index == 0) ? '' : ',' }${dispatcherEntry.value.map.message_mode}"/>
	<c:set var="dispatcherTexts" value="${dispatcherTexts}${(status.index == 0) ? '' : ',' }${dispatcherEntry.value.map.display_name}"/>
</c:forEach>

<body onload="initSenAndRecAutoComp();createToolbar(toolbar);showFilterActive(document.searchForm)" >
<div class="pageHeader">Sent Notifications</div>

<form method="GET" action='<c:out value="${searchUrl}"/>' name="searchForm">

<input type="hidden" name="_method" value="list" id="_method">
<input type="hidden" name="_searchMethod" value="list" id="_searchMethod">
<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
<input type="hidden" name="_hasResult" value="${hasResult}"/>

<!-- new css and new list pattern-->

<insta:search form="searchForm" optionsId="optionalFilter" closed="${hasResult}">
	<div class="searchBasicOpts">
		<div class="sboField">
		<div class="sboFieldLabel">Recipient Id: </div>
			<div id="riecipient_wrapper_r" class="autoComplete">
				<input type="text" name="message_recipient_id" id="user_r" value="" style="width:120px;" />
			<div id="riecipient_dropdown" class="scrolForContainer" style="width:250px"></div>
			</div>
				<input type="hidden" name="user_name_r" id="user_name_r" value=""/>
			<div class="sboFieldLabel" style="display:none">Sender Id: </div>
			<div id="sender_wrapper_s" class="autoComplete" style="display:none">
				<input type="text" name="message_sender_id" id="user_s" value="" style="width:120px;" />
			<div id="sender_dropdown" class="scrolForContainer" style="width:250px"></div>
			</div>
				<input type="hidden" name="user_name_s" id="user_name_s" value=""/>
		</div>

		<div class="sboField">
			<div class="sboFieldLabel">Message Subject : </div>
				<input type="text" name="message_subject" value="${ifn:cleanHtmlAttribute(param.message_subject)}" style="width:120px;" />
				<input type="hidden" name="message_subject@op" value="ilike" />
			</div>
		</div>

		<div class="sboField">
				<div class="sboFieldLabel">Message Type : </div>
					<insta:selectdb name="message_type_name" table="message_types" id="message_type_name"
						dummyvalue="---Select---" dummyvalueId="" value="${message_type_name}"
						valuecol="message_type_name" displaycol="message_type_name" filtercol="message_mode" filtervalue="NOTIFICATION"/>
						<input type="hidden" name="message_type_name@cast" value="y" />
				</div>
		</div>

	</div>
	<div id="optionalFilter" style="clear:both; display:${hasResult ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr>
			<td>
		 		<div class="sfLabel">Notification Date</div>
				<div class="sfField">
					<div class="sfFieldSub">From :</div>
					<insta:datewidget name="last_sent_date" id="last_sent_date0" value="${paramValues.last_sent_date[0]}"/>
				</div>
				<div class="sfField">
					<div class="sfFieldSub">To:</div>
					<insta:datewidget name="last_sent_date" id="last_sent_date1" value="${paramValues.last_sent_date[1]}"/>
					<input type="hidden" name="last_sent_date@op" value="ge,le" />
					<input type="hidden" name="last_sent_date@type" value="date" />
					<input type="hidden" name="last_sent_date@cast" value="y" />
				</div>
			</td>
			<td></td>
		</tr>
	</table>
	</div>
</insta:search>
	<c:if test ="${not empty messageLogList}" >
	<div class="resultList">
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<table class="resultList" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable" >
				<tr>
				    <insta:sortablecolumn name="message_sender_id" title="Sender Id"/>
					<insta:sortablecolumn name="message_recipient_id" title="Recipient Id"/>
					<insta:sortablecolumn name="message_subject" title="Message Subject"/>
					<insta:sortablecolumn name="last_sent_date" title="Notifaction Date"/>
					<insta:sortablecolumn name="message_type_name" title="Message Type"/>
				</tr>
				<c:forEach var="messageLog" items="${messageLogList}" varStatus="status">
					<tr class="${status.index == 0 ?'firstRow': ''} ${status.index % 2 == 0? 'even':'odd' }"
						onclick="showToolbar(${status.index}, event, 'resultTable',
									{message_log_id : '${messageLog.map.message_log_id}'});"
						onmouseover="hideToolBar(${status.index});" id="toolbarRow${status.index}">
						<td>${messageLog.map.message_sender_id} </td>
						<td>${messageLog.map.message_recipient_id} </td>
						<td>${messageLog.map.message_subject}</td>
						<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${messageLog.map.last_sent_date}"/></td>
						<td>${messageLog.map.message_type_name}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:if>

	<table class="screenActions">
		<tr>
			<td>
				<a href="${cpath}/message/MyNotifications.do?_method=list&sortOrder=last_sent_date&sortReverse=true&message_status=S">
				My Notifications
				</a>
				&nbsp;|&nbsp;
				<a href="${cpath}/message/ArchiveMessages.do?_method=list&sortOrder=last_sent_date&sortReverse=true&message_status=">
				Archive Messages
				</a>
			</td>
		</tr>
	</table>
 </form>
<script>

/*
Toolbar for the page
*/
var sendRecNameList = <%= request.getAttribute("nameResourceNameList") %>;
var toolbar = {};
toolbar.ViewMessage = {
	title: "View Message",
	imageSrc: "icons/View.png",
	href: '${detailsUrl}',
	target: '_blank',
	onclick: null,
	description: "View Message"
};
</script>
</body>
</html>
