<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>

<head>
<title>Message Types - Insta HMS</title>
<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
<insta:link type="script" file="ajax.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="css" file="widgets.css"/>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="messageTypeList" value="${pagedList.dtoList}"/>
<c:set var="hasResult" value="${not empty messageTypeList}"/>
<c:set var="searchUrl" value="${cpath}/master/messages/MessageType.do?_method=list" />
<body onload="createToolbar(toolbar);" >

<!--  variable for practo activation status -->
<c:set var="practoSmsMod" value="${practoSmsModule.map.activation_status}"/>
<c:if test="${empty practoSmsMod}"> <c:set var="practoSmsMod" value="N" /> </c:if>


<div class="pageHeader">Message Types</div>

<form method="GET" action="<c:out value='${searchUrl}' />" name="searchForm">

<input type="hidden" name="_method" value="list" id="_method">
<input type="hidden" name="_searchMethod" value="list" id="_searchMethod">
<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverseZ)}"/>
<input type="hidden" name="_hasResult" value="${hasResult}"/>

<!-- new css and new list pattern-->

<c:forEach var="dispatcherEntry" items="${dispatcherMap}" varStatus="status">
	<c:set var="dispatcherValues" value="${dispatcherValues}${(status.index == 0) ? '' : ',' }${dispatcherEntry.value.map.message_mode}"/>
	<c:set var="dispatcherTexts" value="${dispatcherTexts}${(status.index == 0) ? '' : ',' }${dispatcherEntry.value.map.display_name}"/>
</c:forEach>

<insta:search-lessoptions form="searchForm">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel">Message type: </div>
			<div class="sboFieldInput">
				<div id="message_type">
					<insta:selectdb  name="message_type_id" value="${param.message_type_id}" dummyvalue="--Select--" dummyvalueId=""
					table="message_types" valuecol="message_type_id" displaycol="message_type_name"
					/>
				</div>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel">Category: </div>
			<div class="sfFieldInput">
				<div id="message_category">
					<insta:selectdb  name="category_id" value="${param.category_id}" table="message_category"
						valuecol="message_category_id" displaycol="message_category_name" filtered="false"
						dummyvalue="--Select--" dummyvalueId=""/>
					<input type="hidden" name="category_id@cast" value="y">
				</div>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel">Mode: </div>
			<div class="sfFieldInput">
			<div id="message_mode">
				 <insta:checkgroup name="message_mode" opvalues="${dispatcherValues}" optexts="${dispatcherTexts}" selValues="${paramValues.message_mode}"/>
			</div>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel">Status: </div>
			<div class="sfFieldInput">
			<div id="message_status">
				 <insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
			</div>
			</div>
		</div>
	</div>
</insta:search-lessoptions>
	<c:if test ="${not empty messageTypeList}" >
	<div class="resultList">
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<table class="resultList" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable" >
				<tr>
					<insta:sortablecolumn name="message_type_name" title="Message Type"/>
					<th>Category</th>
					<th>Mode</th>
				</tr>

				<c:forEach var="messageType" items="${messageTypeList}" varStatus="status">
					<tr class="${status.index == 0 ?'firstRow': ''}  ${status.index % 2 == 0? 'even':'odd' }"
						onclick="showToolbar(${status.index}, event, 'resultTable',
									{message_type_id : '${messageType.map.message_type_id}'});"
						onmouseover="hideToolBar(${status.index});" id="toolbarRow${status.index}">
						<td>
							<c:if test="${messageType.map.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${messageType.map.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${messageType.map.message_type_name}</td>
						<td>${messageType.map.message_category_name}</td>
						<td>${dispatcherMap[messageType.map.message_mode].map.display_name}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:if>
		<c:url var="addnew" value="${cpath}/master/messages/MessageType.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left">
			<insta:screenlink screenId="mas_message_type" extraParam="?_method=add" label="Add New Message Type"/>
		</div>

		<div class="legend" style="display: ${hasResult? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>
		
<script>	
	var toolbar = {
		View: {
			title: 'View',
			imageSrc: "icons/Edit.png",
			href: 'master/messages/MessageType.do?_method=show',
			onclick: null,
			description: "View Message Type"
		}
	};
	var dispatcherJSON = ${(not empty dispatcherListJSON) ? dispatcherListJSON : '[]'};
</script>
</body>
</html>
