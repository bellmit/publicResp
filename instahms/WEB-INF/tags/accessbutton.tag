<%@tag dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ attribute name="type" required="true" %>
<%@ attribute name="accesskey" required="false" %>
<%@ attribute name="onclick" required="false" %>
<%@ attribute name="styleclass" required="false" %>
<%@ attribute name="display" required="false" %>
<%@ attribute name="disabled" required="false" %> <%-- To make button dissable --%>
<%@ attribute name="id" required="false" %>
<%@ attribute name="name" required="false" %>
<%@ attribute name="buttonkey" required="true" %><fmt:message key="${buttonkey}" var="btnkey"/>

<c:set var="str1" value="${fn:substringBefore(btnkey, '|')}"></c:set>
<c:set var="str2" value="${fn:substringAfter(btnkey, '|')}"></c:set>
<c:set var="str3" value="${fn:substringBefore(str2, '|')}"></c:set>
<c:set var="str4" value="${fn:substringAfter(str2, '|')}"></c:set>

<c:if test="${not empty str3}">
	<c:set var="accessAttr">accesskey="${str3}"</c:set>
</c:if>
<c:if test="${not empty type}">
	<c:set var="typeAttr">type="${type}"</c:set>
</c:if>
<c:if test="${not empty onclick}">
	<c:set var="clickAttr">onclick="${onclick}"</c:set>
</c:if>
<c:if test="${not empty styleclass}">
	<c:set var="styleAttr">class=${styleclass}"</c:set>
</c:if>
<c:if test="${not empty display}">
	<c:set var="titleAttr">title="${display}"</c:set>
</c:if>
<c:if test="${not empty disabled}">
	<c:set var="disabledAttr">disabled="${disabled}"</c:set>
</c:if>
<c:if test="${not empty id}">
	<c:set var="idAttr">id="${id}"</c:set>
</c:if>
<c:if test="${not empty name}">
	<c:set var="nameAttr">name="${name}"</c:set>
</c:if>

<c:set var="accessButton">
<c:out value="${str1}"></c:out><b><u><c:out value="${str3}"/></u></b><c:out value="${str4}"></c:out>
</c:set>

<button ${typeAttr } ${accessAttr } ${clickAttr } ${styleAttr } ${titleAttr } ${disabledAttr } ${idAttr } ${nameAttr }>${accessButton}</button>


<%--
<c:choose>
<c:when test="${btnkey eq '|S|ave'}">
<c:set var="splittedString" value="${fn:split(btnkey, '|')}" />
</c:when>
</c:choose>

		<c:set var="accessButton">

		<c:forEach var="istr" items="${splittedString}" varStatus="istatus">
		<c:set var="i" value="${istatus.count}"/>
		</c:forEach>

		<c:forEach var="str" items="${splittedString}" varStatus="status">

		<c:if test="${i == 2}">

		<c:if test="${status.count == 1}">
    		<b><u><c:out value="${str}"/></u></b>
  		</c:if>

  		<c:if test="${status.count != 1 }">
    		<c:out value="${str}"/>
		</c:if>
		</c:if>
		<c:if test="${i == 3}">
		<c:if test="${status.count == 2}">
    		<b><u><c:out value="${str}"/></u></b>
  		</c:if>

  		<c:if test="${status.count != 2 }">
    		<c:out value="${str}"/>
		</c:if>
		</c:if>

		</c:forEach>

		</c:set>
	<button type="submit" accesskey="S" onclick="return validate();">${accessButton}</button>

<c:set var="acsSaveBtn"><insta:ltext key="patientFeedback.showRating.accessSaveButton.save"/></c:set>

		<c:set var="splittedString" value="${fn:split(acsSaveBtn, '|')}" />
		<c:set var="accessButton">

		<c:forEach var="str" items="${splittedString}" varStatus="status">

		<c:if test="${status.count == 1}">
    		<b><u><c:out value="${str}"/></u></b>
  		</c:if>
  		<c:if test="${status.count != 1}">
    		<c:out value="${str}"/>
  		</c:if>

		</c:forEach>
	</c:set>

	<c:out value="${btnkey}"></c:out>
<c:set var="len" value="${fn:length(btnkey)}" />

<c:set var="firstPipe" value="${fn:indexOf(btnkey, '|')}"></c:set>
<c:out value="${firstPipe}"></c:out>

<c:set var="substr" value="${fn:substring(btnkey, firstPipe+1,firstPipe+2)}"></c:set>
--%>



