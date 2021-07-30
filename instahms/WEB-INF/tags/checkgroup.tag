<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>

<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<%@ attribute name="name" required="true" %>
<%@ attribute name="opvalues" required="true" %>
<%@ attribute name="optexts" required="true" %>
<%@ attribute name="selValues" required="false" type="java.lang.String[]" %>	<%-- array of init values --%>
<%@ attribute name="selValue" required="false" %>		<%-- csv of initial values, takes precedence --%>

<%--
	Generates a group of checkboxes, suitable for a "filter"
	Example Usage:
		<insta:checkgroup name="reg_time" selValues="${paramValues.reg_time}"
				opvalues="Y,N" optexts="Yes,No"/>
	Assumption is that the "all" checkbox has a value of ''
	Also, common.js needs to be included for function onClickCheckGroupAll
--%>

<c:set var="opTextArray" value="${fn:split(optexts,',')}"/>
<c:set var="opValueArray" value="${fn:split(opvalues,',')}"/>
<c:if test="${fn:length(selValue) gt 0}">
  <c:set var="selValues" value="${fn:split(selValue,',')}"/>
</c:if>
<c:set var="all" value="${(empty selValues) || (selValues[0] == '')}"/>

<input type="checkbox" value="" name="${name}" id="${name}_" onclick="enableCheckGroupAll(this)"
	${all ? 'checked' : ''}  isallcheckbox="1"
	<c:forEach items="${dynattrs}" var="a">
 		${ifn:cleanHtmlAttribute(a.key)}="${ifn:cleanHtmlAttribute(a.value)}"
	</c:forEach>
><label for="${name}_">(<insta:ltext key="checkgroup.all"/>)</label><br/>

<c:forEach items="${opValueArray}" var="option" varStatus="status">
	<input type="checkbox" name="${name}" value="${option}" id="${name}_${option}"
		${all ? 'disabled' : (ifn:arrayFind(selValues,option) ne -1) ? 'checked' : ''}
		<c:forEach items="${dynattrs}" var="a">
 			${ifn:cleanHtmlAttribute(a.key)}="${ifn:cleanHtmlAttribute(a.value)}"
		</c:forEach>
	><label for="${name}_${option}">${fn:trim(opTextArray[status.index])}</label><br/>
</c:forEach>

