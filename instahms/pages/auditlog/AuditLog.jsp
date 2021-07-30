<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>

<head>
	<title><insta:ltext key="common.auditlog.list.auditlog.instahms"/></title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="auditlog/auditlog.js"/>
	<insta:link type="css" file="widgets.css"/>
	<style type="text/css">
		.ac {
			width:150px;
			padding-bottom:20px;
		}

 		.yui-skin-sam .yui-ac-content ul {
 			max-height:200px;
			overflow : auto;
 		}
	</style>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="auditLogList" value="${pagedList.dtoList}"/>
<c:set var="hasResult" value="${not empty auditLogList}"/>
<c:set var="urlPrefix" value="${not empty auditType ? auditType : 'master'}"/>
<c:set var="searchParamsUrl" value="${cpath}/${urlPrefix}/auditlog/AuditLogSearch.do?_method=getSearchParams" />
<c:set var="searchUrl" value="${cpath}/${urlPrefix}/auditlog/AuditLogSearch.do" />
<c:set var="detailsUrl" value="${urlPrefix}/auditlog/AuditLogSearch.do?_method=getAuditLogDetails" />

<body onload="init(document.searchForm, '${urlPrefix}');showFilterActive(document.searchForm)" >
<c:set var="operationstatus">
<insta:ltext key="common.auditlog.list.inserts"/>,
<insta:ltext key="common.auditlog.list.updates"/>,
<insta:ltext key="common.auditlog.list.deletes"/>
</c:set>
<c:set var="operationstatus1">
<insta:ltext key="common.auditlog.list.inserts"/>,
<insta:ltext key="common.auditlog.list.updates"/>
</c:set>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<div class="pageHeader"><insta:ltext key="common.auditlog.list.auditlogs"/></div>

<form method="GET" action="<c:out value='${searchUrl}'/>" name="searchForm">

<input type="hidden" name="_method" value="getAuditLogList" id="_method">
<input type="hidden" name="_searchMethod" value="getAuditLogList" id="_searchMethod">
<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
<input type="hidden" name="_hasResult" value="${hasResult}"/>

<c:set var="alTypeValues" value=""/>
<c:set var="alTypeTexts" value=""/>
<c:forEach var="tableEntry" items="${auditLogTables}" varStatus="status">
	<c:if test="${not status.first}">
		<c:set var="alTypeValues" value="${alTypeValues},"/>
		<c:set var="alTypeTexts" value="${alTypeTexts},"/>
	</c:if>
	<c:set var="alTypeValues" value="${alTypeValues}${tableEntry.key}"/>
	<c:set var="alTypeTexts" value="${alTypeTexts}${tableEntry.value}"/>
</c:forEach>
<!-- new css and new list pattern-->

<insta:search form="searchForm" optionsId="optionalFilter" closed="${hasResult}">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="common.auditlog.list.auditlogtype"/>: </div>
			<div class="sboFieldInput">
				<div id="alType">
					<insta:selectoptions name="al_table" id="al_table" onchange="onChangeTable();"
					opvalues="${alTypeValues}" optexts="${alTypeTexts}" value="${param.al_table}"/>
				</div>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="common.auditlog.list.datachanged"/>: </div>
			<div class="sboFieldInput">
				<input type="hidden" name="_selected_field" value="${ifn:cleanHtmlAttribute(param.field_name)}"/>
	    		<select name="field_name" id="field_name" class="dropdown" onchange="onChangeField();">
	    			<option value="">${dummyvalue}</option>
	    		</select>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="common.auditlog.list.valuechanged"/>: </div>
			<div class="sboFieldInput">
				<div class="sfFieldSub"><insta:ltext key="common.auditlog.list.from"/>: </div>
				<div class="sfFieldSub">
				<div id="autofrom" class="ac">
					<input type="text" name="_old_value_input" id="_old_value_input" value="${ifn:cleanHtmlAttribute(param._old_value_input)}"/>
					<input type="hidden" name="old_value" id="old_value" value="${ifn:cleanHtmlAttribute(param.old_value)}"/>
					<div id="_old_value_dropdown" class="ac"></div>
				</div>
				</div>
			</div>
   			<div class="sboFieldInput">
				<div class="sfFieldSub"><insta:ltext key="common.auditlog.list.to"/>: </div>
 				<div class="sfFieldSub">
					<div id="autoto" class="ac">
						<input type="text" name="_new_value_input" id="_new_value_input" value="${ifn:cleanHtmlAttribute(param._new_value_input)}" />
						<input type="hidden" name="new_value" id="new_value" value="${ifn:cleanHtmlAttribute(param.new_value)}"/>
						<div id="_new_value_dropdown" class="ac"></div>
 					</div>
				</div>
			</div>
		</div>
	</div>
	<div id="optionalFilter" style="clear:both; display:${hasResult ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr><td>
			<div class="sfLabel"><insta:ltext key="common.auditlog.list.user"/></div>
			<div class="sfField">
				<div id="alUser">
					<insta:selectdb name="user_name" table="u_user" valuecol="emp_username"
									displaycol="emp_username" filtered="false"  value="${param.user_name}" dummyvalue="${dummyvalue}"
									orderby="emp_username"/>
				</div>
			</div>
		</td>
		<td>
	 		<div class="sfLabel"><insta:ltext key="common.auditlog.list.date"/></div>
			<div class="sfField">
				<div class="sfFieldSub"><insta:ltext key="common.auditlog.list.from"/> :</div>
				<insta:datewidget name="mod_time" id="mod_time0" value="${paramValues.mod_time[0]}"/>
			</div>
			<div class="sfField">
				<div class="sfFieldSub"><insta:ltext key="common.auditlog.list.to"/>:</div>
				<insta:datewidget name="mod_time" id="mod_time1" value="${paramValues.mod_time[1]}"/>
				<input type="hidden" name="mod_time@op" value="ge,le" />
				<input type="hidden" name="mod_time@type" value="date" />
				<input type="hidden" name="mod_time@cast" value="y" />
			</div>
			</td>
			<td>
			<div class="sfLabel"><insta:ltext key="common.auditlog.list.typeofchange"/>: </div>
			<c:choose>
			<c:when test="${auditType eq 'paymentrule'}">
				<div class="sfField">
						 <insta:checkgroup name="operation" opvalues="INSERT,UPDATE,DELETE" optexts="${operationstatus}" selValues="${paramValues.operation}"/>
				</div>
			</c:when>
			<c:otherwise>
				<div class="sfField">
						 <insta:checkgroup name="operation" opvalues="INSERT,UPDATE" optexts="${operationstatus1}" selValues="${paramValues.operation}"/>
				</div>
			</c:otherwise>
			</c:choose>
		</td></tr>
	</table>
	</div>
</insta:search>


	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<c:if test ="${not empty auditLogList}" >
	<div class="resultList">
		<table class="resultList" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable" >
				<tr>
					<c:forEach var="kfEntry" items="${aldesc.keyFields}" varStatus="status">
						<c:set var="kfd" value="${kfEntry.value}"/>
						<c:if test="${not empty kfd}">
							<th>${kfd.displayName}</th>
						</c:if>
					</c:forEach>
					<th><insta:ltext key="common.auditlog.list.who"/></th>
					<th><insta:ltext key="common.auditlog.list.when"/></th>
					<th><insta:ltext key="common.auditlog.list.operation"/></th>
					<th><insta:ltext key="common.auditlog.list.what"/></th>
					<th><insta:ltext key="common.auditlog.list.oldvalue"/></th>
					<th><insta:ltext key="common.auditlog.list.newvalue"/></th>
				</tr>

				<c:forEach var="auditlog" items="${auditLogList}" varStatus="status">
					<tr class="${status.index == 0 ?'firstRow': ''}  ${status.index % 2 == 0? 'even':'odd' }"
						onclick="showToolbar(${status.index}, event, 'resultTable',
								{<c:forEach var='kfEntry' items='${aldesc.keyFields}' varStatus='st'>
									<c:out value='${kfEntry.key}' /> : '${auditlog[kfEntry.key]}',
								</c:forEach>
								al_table: '${ifn:cleanJavaScript(param.al_table)}'
								}, null);"
						onmouseover="hideToolBar(${status.index});" id="toolbarRow${status.index}">
						<c:set var="fdesc" value="${aldesc.fields[auditlog.field_name]}"/>
						<c:set var="vmap" value=""/>
						<c:if test="${not empty fdesc}">
							<c:set var="field_name" value="${fdesc.displayName}"/>
							<c:set var="vmap" value="${fdesc.valueMap}"/>
							<c:if test="${not empty fdesc.lookupDiscriminator && not empty auditlog[fdesc.lookupDiscriminator]}">
								<c:set var="vmap" value="${fdesc.valueMap[auditlog[fdesc.lookupDiscriminator]]}"/>
							</c:if>
						</c:if>
						<c:choose>
						<c:when test="${not empty vmap}">
							<c:set var="old_value" value="${vmap[auditlog.old_value]['VALUE']}"></c:set>
							<c:set var="new_value" value="${vmap[auditlog.new_value]['VALUE']}"></c:set>
						</c:when>
						<c:otherwise>
							<c:set var="old_value" value="${auditlog.old_value}"></c:set>
							<c:set var="new_value" value="${auditlog.new_value}"></c:set>
						</c:otherwise>
						</c:choose>
						<c:forEach var="kfEntry" items="${aldesc.keyFields}" varStatus="status">
							<c:set var="kfd" value="${kfEntry.value}"/>
							<c:if test="${not empty kfd}">
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
						<c:when test="${not empty fdesc}">
						<td>${fdesc.displayName} </td>
						</c:when>
						<c:otherwise>
						<td>${auditlog.field_name} </td>
						</c:otherwise>
						</c:choose>
						<td>${ifn:breakContent(fn:escapeXml(old_value))}</td>
						<td>${ifn:breakContent(fn:escapeXml(new_value))}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:if>
	<c:if test="${param._method == 'getAuditLogList'}">
		<insta:noresults hasResults="${hasResult}"/>
	</c:if>
	</form>
<script>
var toolbar = {};
toolbar.ViewLog = {
	title: "View Log",
	imageSrc: "icons/View.png",
	href: '${detailsUrl}',
	target: '_blank',
	onclick: null,
	description: "View Complete Log"
};
</script>
</body>
</html>
