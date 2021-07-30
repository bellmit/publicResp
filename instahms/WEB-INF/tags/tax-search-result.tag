<%@ tag body-content="empty" dynamic-attributes="dynatr"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib prefix="insta" tagdir="/WEB-INF/tags"%>
<%@ attribute name="dataList" required="true"
	type="com.insta.hms.common.PagedList"%>
<%@ attribute name="columns" required="true"%>
<%@ attribute name="keyField" required="true"%>
<%@ attribute name="mappingFieldName" required="true"%>
<%@ attribute name="mappingFieldValue" required="true"%>
<%@ attribute name="sortableColumns" required="false"%>
<%@ attribute name="showToolbar" required="false"%>
<%@ attribute name="statusFlags" required="false"%>
<%@ attribute name="statusCol" required="false"%>

<%--
Parameters :
dataList : PagedList object containing the search results
columns : String formatted as <field_name>:<display_name>,<field_name>:<display_name>
	E.g., "plan_name:Plan Name,plan_copay:Copay %"
keyField : field_name of the primary key column, used in the toolbar to link to toolbar action
sortableColumns : comma separated list of column names that are sortable
showToolbar : Y / N  indicating if a toolbar is required. 'N' by default.
statusFlags : status code and flag images as key : value pairs
statusCol : column name in the dataList record that indicates the status (for mapping to the flag to be displayed) 
--%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${empty dataList.dtoList ? false : true}" />
<c:set var="colArray" value="${fn:split(columns, ',')}" />
<c:set var="mappingArray" value="${fn:split(mappingFieldValue, ',')}" />
<c:set var="sortables"
	value="${null != sortableColumns ? fn:split(sortableColumns, ',') : ''}" />
<c:set var="keyArray" value="${fn:split(keyField, ',')}" />
<c:if test="${empty statusFlags}">
	<c:set var="statusFlags"
		value="I:grey_flag.gif,A:empty_flag.gif,C:red_flag.gif,X:yellow_flag.gif,F:blue_flag.gif,P:yellow_flag.gif" />
	<c:set var="flagArray" value="${fn:split(statusFlags,',')}" />
</c:if>
<c:if test="${empty statusCol}">
	<c:set var="statusCol" value="status" />
</c:if>
<c:if test="${empty showToolbar}">
	<c:set var="showToolbar" value="N" />
</c:if>
<insta:paginate curPage="${dataList.pageNumber}"
	numPages="${dataList.numPages}" totalRecords="${dataList.totalRecords}" />
<div class="resultList">
	<table class="resultList" cellspacing="0" cellpadding="0"
		id="resultTable" onmouseover="hideToolBar('');">
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<c:forEach items="${colArray}" varStatus="st">
				<c:set var="colParts" value="${fn:split(colArray[st.index],':')}" />
				<c:set var="fieldName"
					value="${ fn:length(colParts) > 0 ? colParts[0] : '' }" />
				<c:set var="displayName"
					value="${(null == colParts) ? '' : 
							(fn:length(colParts) > 1) ? colParts[1] : 
							ifn:prettyPrint(colParts[0]) }" />
				<c:choose>
					<c:when test="${-1 != ifn:arrayFind(sortables, fieldName)}">
						<insta:sortablecolumn name="${fieldName}" title="${displayName}" />
					</c:when>
					<c:otherwise>
						<th>${displayName}</th>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		</tr>

		<c:forEach var="record" items="${dataList.dtoList}" varStatus="st">
			<tr
				class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
				onclick="showToolbar(${st.index}, event, 'resultTable',
				{${keyArray[0]}: '${record[keyArray[0]]}',${keyArray[1]}: '${record[keyArray[1]]}'},'');"
				id="toolbarRow${st.index}">
				<td>${(dataList.pageNumber-1) * dataList.pageSize + st.index + 1 }</td>
				<c:forEach items="${colArray}" var="desc" varStatus="s">
					<td class="${showToolbar == 'N' ? 'noToolbar' : ''}">
					  <c:set var="colParts" value="${fn:split(colArray[s.index],':')}" />
					  <c:set var="fieldName" value="${ fn:length(colParts) > 0 ? colParts[0] : '' }" /> 
					  <c:if test="${s.index == 0}">
							<c:forEach items="${flagArray}" var="sflag" varStatus="t">
								<c:set var="flagParts" value="${fn:split(sflag,':')}" />
								<c:if test="${record[statusCol] eq flagParts[0]}">
									<img src='${cpath}/images/${flagParts[1]}'>
								</c:if>
							</c:forEach>
					  </c:if>
					  <c:choose>
					    <c:when test="${fieldName == mappingFieldName }">
					    	<c:set var="fieldValue" value="${record[fieldName]}"/>
					    	<c:forEach items="${mappingArray}" var="fieldMapping" varStatus="i" >
					    	   <c:set var="mappingIndexVal" value="${fn:split(mappingArray[i.index],':')}" /> 
					           <c:set var="mappingKey" value="${ fn:length(mappingIndexVal) > 0 ? mappingIndexVal[0] : '' }" />
					           <c:set var="mappingKeyValue" value="${ fn:length(mappingIndexVal) > 0 ? mappingIndexVal[1] : '' }" /> 
					           <c:if test="${mappingKey == fieldValue}">
					             ${mappingKeyValue}
					           </c:if>
					    	</c:forEach>
					    </c:when>
					    <c:otherwise>
					    	${ifn:cleanHtml(record[fieldName])}
					    </c:otherwise>
					  </c:choose>
				    </td>
				</c:forEach>
			</tr>
		</c:forEach>

	</table>

	<c:if test="${param._method == 'list'}">
		<insta:noresults hasResults="${hasResults}" />
	</c:if>
</div>
