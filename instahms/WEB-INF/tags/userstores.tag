<%@ tag body-content="empty" dynamic-attributes="dynatr" pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ attribute name="username" required="true" %>
<%@ attribute name="elename" required="true" %>
<%@ attribute name="val" required="false" %>
<%@ attribute name="defaultVal" required="false" %>
<%@ attribute name="onlySuperStores" required="false"%>
<%@ attribute name="onlyWithCounters" required="false"%>
<%@ attribute name="onlySalesStores" required="false"%>
<%@ attribute name="allowedRaiseBill" required="false"%>
<%@ attribute name="multipleSelect" required="false"%>
<%@ attribute name="showDefaultValueForNormalUsers" required="false"%>
<%@ attribute name="sterileStores" required="false"%>
<%@ attribute name="storesWithTariff" required="false"%>
<%
if (onlySuperStores == null || onlySuperStores.equals("")) onlySuperStores = "N";
if (onlyWithCounters == null || onlyWithCounters.equals("")) onlyWithCounters = "N";
if (onlySalesStores == null || onlySalesStores.equals("")) onlySalesStores = "N";
if (allowedRaiseBill == null || allowedRaiseBill.equals("")) allowedRaiseBill = "N";

	int roleId = com.insta.hms.stores.StoresDBTablesUtil.getRoleForUser(username);
	java.util.List li  =  com.insta.hms.stores.StoresDBTablesUtil.getLoggedUserStores(username, onlySuperStores, onlyWithCounters, allowedRaiseBill, onlySalesStores, sterileStores, storesWithTariff);
	request.setAttribute("li", li);
	request.setAttribute("liJSON", new flexjson.JSONSerializer().exclude("class").serialize(com.insta.hms.common.ConversionUtils.copyListDynaBeansToMap(li)));
	java.util.List fullli  =  com.insta.hms.stores.StoresDBTablesUtil.getStores(onlySuperStores, onlyWithCounters, allowedRaiseBill, onlySalesStores, sterileStores, storesWithTariff);
	request.setAttribute("fullli", fullli);
	request.setAttribute("fullliJSON", new flexjson.JSONSerializer().exclude("class").serialize(com.insta.hms.common.ConversionUtils.copyListDynaBeansToMap(fullli)));
	request.setAttribute("multipleSelect", multipleSelect);
%>
<c:set var="storevalue" value="${not empty val ? val : ((not empty showDefaultValueForNormalUsers && showDefaultValueForNormalUsers eq 'Y') ? '-9' : pharmacyStoreId)}"/>

<c:choose>
	<c:when test="${roleId eq 1 || roleId eq 2}">
	<c:set var="usersStoresList" value="${fullliJSON}" scope="request"/>
		<select name="${elename}"
		<c:forEach items="${dynatr}" var="a">${a.key}="${ifn:cleanHtmlAttribute(a.value)}"</c:forEach>
		<c:if test="${not empty multipleSelect}">multiple  size="${multipleSelect}"></c:if>
		<c:if test="${empty multipleSelect}">class="dropDown"></c:if>
			<c:if test="${not empty defaultVal}">
				<option value="">${defaultVal}</option>
			</c:if>
			<c:forEach  var="str" items="${fullli}">
		 		<option value='${str.map.dept_id }' onmouseover='this.title = "${fn:escapeXml(str.map.dept_name)}"' <c:if test="${str.map.dept_id eq storevalue}">selected</c:if>>${str.map.dept_name}</option>
		 	</c:forEach>
		 </select>
	 </c:when>
	 <c:otherwise>
	 <c:set var="usersStoresList" value="${liJSON}" scope="request"/>
		<select name="${elename}"
		<c:forEach items="${dynatr}" var="a">${a.key}="${ifn:cleanHtmlAttribute(a.value)}"</c:forEach>
		<c:if test="${not empty multipleSelect}">multiple size="${multipleSelect}"></c:if>
		<c:if test="${empty multipleSelect}">class="dropDown"> </c:if>
			<c:if test="${not empty showDefaultValueForNormalUsers && not empty defaultVal && showDefaultValueForNormalUsers eq 'Y'}">
				<option value="">${defaultVal}</option>
			</c:if>
			<c:forEach var="st" items="${li}">
		 		<option value='${st.map.dept_id }'<c:if test="${st.map.dept_id eq storevalue}">selected</c:if>>${st.map.dept_name}</option>
		 	</c:forEach>
		 </select>
	 </c:otherwise>
</c:choose>