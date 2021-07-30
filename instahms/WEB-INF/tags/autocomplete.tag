<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>

<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ attribute name="name" required="false" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="value" required="false" %>
<%@ attribute name="jsvar" required="true"%>
<%@ attribute name="common_datasource" required="false"%>
<%@ attribute name="maxlength" required="false" %>

<%--
usage <insta:autocomplete id="testnameac1" name="testnameac1"  jsvar="complaints" common_datasource="true"/>
--%>


<div id="autocomplete_${id}" class="autocomplete_widget">
	<input type="text" id="${id}" maxlength="${maxlength}"
	<c:if test="${not empty name}" >
	name="${name}"</c:if>  jsvar="${jsvar}"
	<c:choose>
		<c:when test="${not empty common_datasource}">common_datasource="${common_datasource}"</c:when>
		<c:otherwise >common_datasource="true"</c:otherwise>
	</c:choose>

	displayValue="${value}" value="${value}"/>
	<div id="container_${id}"></div>
</div>





