<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>

<head>
	<title>Message Log - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="messaging/messaging.js"/>
	<insta:link type="css" file="widgets.css"/>
	<style type="text/css">
		.failedmsg {
			color:red;
		}
		.refreshButton{
			cursor:pointer;margin-top:2px;height:10px;
		}
		.refreshLog{
			float:left;color:#bdbdbd;margin-right: 15px;
		}
		.refreshLink{
			cursor:pointer;
		}
	</style>
<script>
  contextPath = "${pageContext.request.contextPath}";
  publicPath = contextPath + "/ui/";
</script>	
</head>
<c:set var="practoSmsMod" value="${practoSmsModule.map.activation_status}"/>
<c:if test="${empty practoSmsMod}"> <c:set var="practoSmsMod" value="N" /> </c:if>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="messageLogList" value="${pagedList.dtoList}"/>
<c:set var="hasResult" value="${not empty messageLogList}"/>
<c:set var="urlPrefix" value="${not empty messageGroup ? messageGroup : 'master'}"/>
<c:set var="searchUrl" value="${cpath}/message/MessageLog.do?_method=list" />
<c:set var="detailsUrl" value="message/MessageLog.do?_method=show" />

<c:forEach var="dispatcherEntry" items="${dispatcherMap}" varStatus="status">
	<c:set var="dispatcherValues" value="${dispatcherValues}${(status.index == 0) ? '' : ',' }${dispatcherEntry.value.map.message_mode}"/>
	<c:set var="dispatcherTexts" value="${dispatcherTexts}${(status.index == 0) ? '' : ',' }${dispatcherEntry.value.map.display_name}"/>
</c:forEach>

<body onload="createToolbar(toolbar);showFilterActive(document.searchForm);initSenAndRecAutoComp()" >
<div class="pageHeader">Message Log</div>

<form method="GET" action='<c:out value="${searchUrl}"/>' name="searchForm">

<input type="hidden" name="_method" value="list" id="_method">
<input type="hidden" name="_searchMethod" value="list" id="_searchMethod">
<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
<input type="hidden" name="_hasResult" value="${hasResult}"/>

<!-- new css and new list pattern-->

<insta:search form="searchForm" optionsId="optionalFilter" closed="${hasResult}" validateFunction="validateDateRange()">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel">Message Type: </div>
			<div class="sboFieldInput">
				<div id="message_type">
					<insta:selectdb  name="message_type_id" value="${message_type_id}"
					table="message_types" valuecol="message_type_id" displaycol="message_type_name"
					filtered="true" dummyvalue="----Select----" dummyvalueId=""/>
				</div>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel">Category: </div>
			<div class="sfFieldInput">
				<div id="message_category">
					<insta:selectdb  name="category_id" value="${category_id}" table="message_category"
						valuecol="message_category_id" displaycol="message_category_name"
						filtered="false" dummyvalue="----Select----" dummyvalueId=""/> 
						<input type="hidden" name="category_id@cast" value="y"/> 
				</div>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel">Sender Id: </div>
			<div id="sender_wrapper_s" class="autoComplete">
				<input type="text" name="message_sender_id" id="user_s" value="" style="width:120px;" />
			<div id="sender_dropdown" class="scrolForContainer" style="width:250px"></div>
			</div>
				<input type="hidden" name="user_name_s" id="user_name_s" value=""/>
			</br>
			</br>
			<div class="sboFieldLabel">Recipient Id: </div>
			<div id="riecipient_wrapper_r" class="autoComplete">
				<input type="text" name="message_recipient_id" id="user_r" value="" style="width:120px;" />
			<div id="riecipient_dropdown" class="scrolForContainer" style="width:250px"></div>
			</div>
				<input type="hidden" name="user_name_r" id="user_name_r" value=""/>
		</div>

		<div class="sboField">
			<div class="sboFieldLabel">Mode: </div>
			<div class="sfFieldInput">
			<div id="message_mode">
				 <insta:checkgroup name="message_mode" opvalues="${dispatcherValues}" optexts="${dispatcherTexts}" selValues="${paramValues.message_mode}"/>
			</div>
			</div>
		</div>
	</div>
	<div id="optionalFilter" style="clear:both; display:${hasResult ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel">Delivery Status: </div>
				<div class="sfField">
					<c:if test="${practoSmsMod eq 'N'}">
					<insta:checkgroup name="last_status" opvalues="S,F" optexts="Sent,Failed" selValues="${paramValues.last_status}"/>
					</c:if>
					<c:if test="${practoSmsMod eq 'Y'}">
					<insta:checkgroup name="last_status" opvalues="S,F,R" optexts="Sent,Failed,Delivered" selValues="${paramValues.last_status}"/>
					</c:if>
				</div>
			</td>

			<td>
		 		<div class="sfLabel">Last Sent Date</div>
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
		</tr>
	</table>
	</div>
</insta:search>
	<c:if test ="${not empty messageLogList}" >
	<div class="resultList">
	<c:if test="${practoSmsMod eq 'Y'}">
		<div class="refreshLog"><a onClick="getUpdatedStatus();" class="refreshLink"><img src='${cpath}/icons/Refresh.png' class="refreshButton" >Refresh Log</a>  
		<c:if test="${lastRunTime != ''}">
		Last Refreshed at ${lastRunTime}
		</c:if>
		</div>
	</c:if>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<table class="resultList" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable" >
				<tr>
					<insta:sortablecolumn name="message_type_name" title="Message Type"/>
					<insta:sortablecolumn name="message_category_name" title="Category"/>
					<insta:sortablecolumn name="message_mode" title="Mode"/>
					<insta:sortablecolumn name="last_sent_date" title="Sent Date"/>
					<th>Last Status</th>
					<th>Retry Count</th>
					<insta:sortablecolumn name="message_sender_id" title="Sender Id"/>
					<insta:sortablecolumn name="message_recipient_id" title="Recipient Id"/>
				</tr>

				<c:forEach var="messageLog" items="${messageLogList}" varStatus="status">
					<tr class="${status.index == 0 ?'firstRow': ''} ${status.index % 2 == 0? 'even':'odd' }"
						onclick="showToolbar(${status.index}, event, 'resultTable',
									{message_log_id : '${messageLog.map.message_log_id}'});"
						onmouseover="hideToolBar(${status.index});" id="toolbarRow${status.index}">
						<td>${messageLog.map.message_type_name}</td>
						<td>${messageLog.map.message_category_name}</td>
						<td>${dispatcherMap[messageLog.map.message_mode].map.display_name}</td>
						<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${messageLog.map.last_sent_date}"/></td>
						<td>
						<c:if test="${messageLog.map.last_status eq 'F'}"><img src='${cpath}/icons/failed.png' style="height:15px;width:15px"> Failed</c:if>
						<c:if test="${messageLog.map.last_status eq 'S'}"><img src='${cpath}/icons/sent.png' style="height:15px;width:15px"> Sent </c:if>
						<c:if test="${messageLog.map.last_status eq 'R'}"><img src='${cpath}/icons/delivered.png' style="height:15px;width:15px"> Delivered </c:if>
						</td>
						<td>${messageLog.map.retry_count == 0 ? '' : messageLog.map.retry_count}</td>
						<td>${messageLog.map.message_sender_id} </td>
						<td>${messageLog.map.message_recipient_id} </td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:if>
	<insta:noresults hasResults="${hasResult}"/>
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
