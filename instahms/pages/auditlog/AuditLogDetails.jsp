<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>

<head>
	<title><insta:ltext key="common.auditlog.auditlogdetails.auditlogdetails.instahms"/></title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<style>
		.keystring {
		 font-weight : normal;
		 }
	</style>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="auditLogList" value="${pagedList.dtoList}"/>
<c:set var="hasResult" value="${not empty auditLogList}"/>
<c:set var="showmsg">
<insta:ltext key="common.auditlog.auditlogdetails.messagelog"/>
</c:set> 
<body>
<div class="pageHeader">${auditLogTables[param.al_table]} <insta:ltext key="common.auditlog.auditlogdetails.auditlog"/></div>
<c:set var="hasBaseTable" value="0"/>

<div>
<c:forEach var="keyFieldMap" items="${aldesc.keyFields}" varStatus="status">
	<c:set var="kfName" value="${keyFieldMap.key}"/>
	<c:set var="kfd" value="${keyFieldMap.value}"/>

	<c:if test="${not empty kfd}">
		<c:set var="vmap" value="${kfd.valueMap}"/>
		<c:set var="key_value" value="${param[kfd.fieldName]}"/>
		<c:if test="${not empty vmap && not empty vmap[param[kfd.fieldName]]}">
			<c:set var="key_value" value="${vmap[param[kfd.fieldName]]['VALUE']}"/>
		</c:if>

		<c:if test="${not empty key_value}">
			${kfd.displayName}: <b>${ifn:cleanHtml(key_value)}</b>&nbsp;
		</c:if>
	</c:if>
	<c:if test="${kfName eq 'base_table'}">
		<c:set var="hasBaseTable" value="1"/>
	</c:if>
</c:forEach>
</div>
<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
<c:if test ="${not empty auditLogList}" >
<div class="resultList">
	<table class="resultList" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable">
			<tr>
				<c:forEach var="kfEntry" items="${aldesc.keyFields}" varStatus="status">
					<c:set var="kfd" value="${kfEntry.value}"/>
					<c:if test="${not empty kfd && kfd.displayAsKey}">
						<th>${kfd.displayName}</th>
					</c:if>
				</c:forEach>
				<th><insta:ltext key="common.auditlog.auditlogdetails.who"/></th>
				<th><insta:ltext key="common.auditlog.auditlogdetails.when"/></th>
				<th><insta:ltext key="common.auditlog.auditlogdetails.operation"/></th>
				<th><insta:ltext key="common.auditlog.auditlogdetails.what"/></th>
				<th><insta:ltext key="common.auditlog.auditlogdetails.oldvalue"/></th>
				<th><insta:ltext key="common.auditlog.auditlogdetails.newvalue"/></th>
			</tr>

			<c:set var="prevBaseTable" value=""/>
			<c:set var="prevKeySet" value=""/>
			<c:set var="prevUser" value=""/>
			<c:set var="prevTimeStamp" value=""/>
			<c:set var="prevOperation" value=""/>
			<c:forEach var="auditlog" items="${auditLogList}" varStatus="status">
				<c:set var="printHeader" value="0"/>
				<c:set var="currentBaseTable" value="${hasBaseTable == 1 ? auditlog['base_table'] : ''}"/>
				<c:set var="currentKeySet" value=""/>
				<c:set var="currentKeyString" value=""/>
				<c:set var="detailsUrl"
					value="AuditLogSearch.do?_method=getAuditLogDetails&al_table=${currentBaseTable}"/>

				<c:forEach var="keyFieldMap" items="${aldesc.keyFields}" varStatus="keyStatus">
				<c:set var="kfName" value="${keyFieldMap.key}"/>
				<c:set var="kfd" value="${keyFieldMap.value}"/>
				<c:if test="${kfName ne 'base_table'}">
					<c:set var="auditKeyValue" value="${auditlog[kfName]}"/>
					<c:if test="${not empty auditKeyValue}">
					<c:set var="detailsUrl">${detailsUrl}&amp;<c:out value='${kfName}'/>=${auditKeyValue}</c:set>
					</c:if>
					<c:set var="vmap" value="${kfd.valueMap}"/>
					<c:if test="${not empty vmap && not empty vmap[auditKeyValue]}">
					<c:set var="auditKeyValue" value="${vmap[auditKeyValue]['VALUE']}"/>
					</c:if>
					<c:set var="currentKeySet" value="${currentKeySet}.${auditlog[kfName]}"/>
					<c:set var="keyString" value="${not empty currentKeyString ? '' : auditLogTables[currentBaseTable]}-&nbsp;${auditKeyValue}&nbsp;"/>
					<c:set var="currentKeyString"
						value="${currentKeyString}${(not empty kfd && empty param[kfd.fieldName] && !kfd.displayAsKey && not empty auditKeyValue && not empty auditLogTables[currentBaseTable]) ? keyString : ''}"/>
				</c:if>
				</c:forEach>
				<c:if test="${ (prevBaseTable ne currentBaseTable) || (prevKeySet ne currentKeySet) ||
							(prevUser ne auditlog.user_name) || (prevTimeStamp ne auditlog.mod_time) ||
							(prevOperation ne auditlog.operation) }">
					<c:set var="printHeader" value="1"/>
				</c:if>

				<c:set var="old_value" value="${auditlog.old_value}"/>
				<c:set var="new_value" value="${auditlog.new_value}"/>
				<c:set var="field_name" value="${auditlog.field_name}"/>
				<c:set var="fd" value="${aldesc.fields[auditlog.field_name]}"/>
				<c:set var="vmap" value=""/>
				<c:if test="${not empty fd}">
					<c:set var="field_name" value="${fd.displayName}"/>
					<c:set var="vmap" value="${fd.valueMap}"/>
					<c:if test="${not empty fd.lookupDiscriminator && not empty auditlog[fd.lookupDiscriminator]}">
						<c:set var="vmap" value="${fd.valueMap[auditlog[fd.lookupDiscriminator]]}"/>
					</c:if>
				</c:if>
				<c:if test="${not empty vmap}">
					<c:set var="old_value" value="${vmap[auditlog.old_value]['VALUE']}"></c:set>
					<c:set var="new_value" value="${vmap[auditlog.new_value]['VALUE']}"></c:set>
				</c:if>
				<c:choose>
				<c:when test="${printHeader == 1}">
			  <tr class=class="${(status.index == 0) ? 'firstRow' : ''}">
			  		<c:forEach var="kfEntry" items="${aldesc.keyFields}" varStatus="status">
						<c:set var="kfd" value="${kfEntry.value}"/>
						<c:if test="${not empty kfd && kfd.displayAsKey}">
							<c:set var="column_value">${auditlog[kfd.fieldName]}</c:set>
							<c:if test="${not empty kfd.valueMap && not empty kfd.valueMap[column_value]}">
								<c:set var="column_value" value="${kfd.valueMap[column_value]['VALUE']}"/>
							</c:if>
							<td>${column_value}</td>
						</c:if>
					</c:forEach>
					<td>${auditlog.user_name}</td>
					<td><fmt:formatDate value="${auditlog.mod_time}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
					<td>${auditlog.operation}</td>
					<c:choose>
					<c:when test="${not empty currentKeyString}">
					<td colspan="3" class="keystring"><a href="${detailsUrl}">${currentKeyString}</a></td>
					</c:when>
					<c:otherwise>
					<td>${field_name}</td>
					<td>${ifn:breakContent(fn:escapeXml(old_value))}</td>
					<td>${ifn:breakContent(fn:escapeXml(new_value))}</td>
					</c:otherwise>
					</c:choose>
				</tr>
				<tr class="${status.index == 0 ?'firstRow': ''}">
					<c:forEach var="kfEntry" items="${aldesc.keyFields}" varStatus="status">
						<c:set var="kfd" value="${kfEntry.value}"/>
						<c:if test="${not empty kfd && kfd.displayAsKey}">
							<td></td>
						</c:if>
					</c:forEach>
					<td></td>
					<td></td>
					<td></td>
					<td>${field_name}</td>
					<td>${ifn:breakContent(fn:escapeXml(old_value))}</td>
					<td>${ifn:breakContent(fn:escapeXml(new_value))}</td>
				</tr>
				</c:when>
				<c:otherwise>
				<tr class="${status.index == 0 ?'firstRow': ''}">
					<c:forEach var="kfEntry" items="${aldesc.keyFields}" varStatus="status">
						<c:set var="kfd" value="${kfEntry.value}"/>
						<c:if test="${not empty kfd && kfd.displayAsKey}">
							<td></td>
						</c:if>
					</c:forEach>
					<td></td>
					<td></td>
					<td></td>
					<td>${field_name}</td>
					<td>${ifn:breakContent(fn:escapeXml(old_value))}</td>
					<td>${ifn:breakContent(fn:escapeXml(new_value))}</td>
				</tr>
				</c:otherwise>
				</c:choose>
				<c:set var="prevOperation" value="${auditlog.operation}"/>
				<c:set var="prevTimeStamp" value="${auditlog.mod_time}"/>
				<c:set var="prevUser" value="${auditlog.user_name}"/>
				<c:set var="prevKeySet" value="${currentKeySet}"/>
				<c:set var="prevBaseTable" value="${currentBaseTable}"/>
			</c:forEach>
		</table>
	</div>
</c:if>

<insta:noresults hasResults="${hasResult}" message="${showmsg}"/>
<c:if test="${not empty param.showBackLink}">
	<div class="screenActions">
		<a href="#" onclick="window.location.href='<c:out value='${header.referer}' />'">${ifn:cleanHtml(param.showBackLink)}</a>
		</div>
		</c:if>
</body>
</html>
