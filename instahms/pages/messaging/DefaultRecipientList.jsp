<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>

<head>
	<title>Send Message - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="messaging/messaging.js"/>
	<insta:link type="css" file="widgets.css"/>
	<script>
	  contextPath = "${pageContext.request.contextPath}";
	  publicPath = contextPath + "/ui/";
	</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResult" value="${not empty messageDataList}"/>
<c:set var="urlPrefix" value="${not empty messageGroup ? messageGroup : 'master'}"/>

<body onload="onLoadRecipientList();">
<div class="pageHeader">Send Message</div>
<div class="fieldSetLabel">
	Select Recipients :
	<c:forEach var="provider" items="${providerList}" varStatus="pstatus">
		<c:choose>
			<c:when test="${provider ne _currentProvider}">
			<a href="#" onclick="selectProvider('${provider}');">${provider}</a>
			</c:when>
			<c:otherwise>
			${provider}
			</c:otherwise>
		</c:choose>
		&nbsp;&nbsp;
	</c:forEach>
</div>
<div>&nbsp;</div>
<form method="POST" action="Message.do" name="MessageForm">

<input type="hidden" name="_method" value="saveRecipients" id="_method">
<!--
<input type="hidden" name="prevProvider" value="${_currentProvider}"/>
-->
<input type="hidden" name="_currentProvider" value="${_currentProvider}"/>
<input type="hidden" name="_nextProvider" value=""/>
<input type="hidden" name="_removed_selections" value=""/>
<!-- new css and new list pattern-->
	<c:if test ="${not empty messageDataList}" >
	<c:set var="dataMap" value="${messageDataList[0]}"/>
	<div class="resultList">
		<insta:paginate curPage="${pagingInfo['pageNumber']}" numPages="${pagingInfo['numPages']}" totalRecords="${pagingInfo['totalRecords']}"/>
		<table class="resultList dialog_displayColumns" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable" >
				<tr>
					<c:set var="all_checked" value=""/>

					<c:if test="${_select_all}">
						<c:set var="all_checked" value="checked"/>
					</c:if>
					<th><input type="checkbox" name="_select_all"
						value="true" onclick="allRecipients();" ${all_checked}/></th>
					<c:forEach var="token" items="${tokenList}">
					<c:if test ="${token != 'key' &&  !fn:startsWith(token, '_')}" >
					<th>${ifn:prettyPrint(token)}</th>
					</c:if>
					</c:forEach>
				</tr>
				<c:forEach var="recipientData" items="${messageDataList}" varStatus="status">
					<c:set var="checked" value=""/>
					<tr class="${status.index == 0 ?'firstRow': ''} ${status.index % 2 == 0? 'even':'odd' }">
						<c:forEach var="selection" items="${currentSelections}" varStatus="selStatus">
							<c:if test="${selection eq recipientData.key}">
							<c:set var="checked" value="checked"/>
							</c:if>
						</c:forEach>
						<td><input type="checkbox" name="_selected_recipients"
								value="${recipientData.key}" ${checked} onclick="clickRecipient(this, '${recipientData.key}');"/></td>

						<c:forEach var="token" items="${tokenList}">
						<c:if test ="${token != 'key'  &&  !fn:startsWith(token, '_')}" >
						<td>
						<c:if test="${token eq 'receipient_name'}">
						<c:if test="${messageLog.map.message_mode eq 'SMS' && empty recipientData.recipient_mobile}"><img src='${cpath}/images/red_flag.gif'></c:if>
						<c:if test="${messageLog.map.message_mode eq 'EMAIL' && empty recipientData.recipient_email}"><img src='${cpath}/images/red_flag.gif'></c:if>
						</c:if>
						${recipientData[token]}
						</td>
						</c:if>
						</c:forEach>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:if>
	<insta:noresults hasResults="${hasResult}"/>
	<div class="screenActions">
	<button type="button"  class="button" accesskey="N"  onclick="return saveRecipients('${_currentProvider}');"><b><u>N</u></b>ext</button>
	<div class="legend" style="display: ${hasResult? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText">Recipient without contact information </div>
	</div>
	</div>
	</form>
</body>
</html>
